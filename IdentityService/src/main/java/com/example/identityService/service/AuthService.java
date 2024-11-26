package com.example.identityService.service;

import com.example.identityService.DTO.EmailEnum;
import com.example.identityService.DTO.request.EmailRequest;
import com.example.identityService.DTO.request.ChangePasswordRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.request.UpdateProfileRequest;
import com.example.identityService.DTO.response.CloudResponse;
import com.example.identityService.DTO.response.LoginResponse;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.Util.TimeConverter;
import com.example.identityService.entity.Account;
import com.example.identityService.entity.Logs;
import com.example.identityService.DTO.Token;
import com.example.identityService.entity.Role;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.mapper.CloudImageMapper;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.LoggerRepository;
import com.example.identityService.repository.RoleRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    @NonFinal
    @Value(value = "${app.baseUrl}")
    private String APP_BASEURL;

    @NonFinal
    @Value(value = "${security.authentication.max-login-attempt}")
    private Integer MAX_LOGIN_ATTEMPT;
    @NonFinal
    @Value(value = "${security.authentication.login-delay-fail}")
    private String LOGIN_DELAY_FAIL;
    @NonFinal
    @Value(value = "${security.authentication.max-forgot-password-attempt}")
    private Integer MAX_FORGOT_PASSWORD_ATTEMPT;
    @NonFinal
    @Value(value = "${security.authentication.delay-forgot-password}")
    private String DELAY_FORGOT_PASSWORD;

    @NonFinal
    @Value(value = "${security.authentication.jwt.access-token-life-time}")
    private String ACCESS_TOKEN_LIFE_TIME;
    @NonFinal
    @Value(value = "${security.authentication.jwt.refresh-token-life-time}")
    private String REFRESH_TOKEN_LIFE_TIME;
    @NonFinal
    @Value(value = "${security.authentication.jwt.email-token-life-time}")
    private String EMAIL_TOKEN_LIFE_TIME;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    private final CloudinaryService cloudinaryService;
    private final CloudImageMapper cloudImageMapper;
    private final EmailService emailService;
    private final LoggerRepository loggerRepository;
    private final RoleRepository roleRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private final AccountMapper accountMapper;

    // -----------------------------Login logout start-------------------------------
    public LoginResponse login(LoginRequest request, String ip){
        Account account = getAccountByEmail(request.getEmail());
        if(!account.isVerified()) throw new AppExceptions(ErrorCode.NOT_VERIFY_ACCOUNT);
        boolean success = passwordEncoder.matches(request.getPassword(), account.getPassword());
        if(!success){
            String key = String.join("","login-attempt:", account.getEmail());
            String attemptTimeString = redisTemplate.opsForValue()
                    .get(key);

            if(attemptTimeString != null && Integer.parseInt(attemptTimeString) == MAX_LOGIN_ATTEMPT)
                throw new AppExceptions(ErrorCode.TOO_MUCH_LOGIN_FAIL);
            int value = attemptTimeString != null ? Integer.parseInt(attemptTimeString) + 1 : 1;
            redisTemplate.opsForValue().set(key, Integer.toString(value),
                    Duration.ofMillis(TimeConverter.convertToMilliseconds(LOGIN_DELAY_FAIL)));
            throw new AppExceptions(ErrorCode.INVALID_EMAIL_PASSWORD);
        }

        return loginProcess(account, ip);
    }

    public LoginResponse loginProcess(Account account, String ip){
        boolean isNewIp = !loggerRepository.existsByEmailAndIp(account.getEmail(),ip);
        if(isNewIp){
            sendConfirmValidIp(account.getEmail(), ip);
        }

        String accessToken = tokenService.accessTokenFactory(account);
        String refreshToken = tokenService.generateRefreshToken(account.getEmail(), ip);
        loggerRepository.save(Logs.builder()
                .actionName("LOGIN")
                .ip(ip)
                .build());
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Boolean logout(String accessToken, String refreshToken) {
        boolean isDisabledAccessToken = tokenService.deActiveToken(new Token(accessToken,
                TimeConverter.convertToMilliseconds(ACCESS_TOKEN_LIFE_TIME)));
        boolean isDisabledRefreshToken = tokenService.deActiveToken(new Token(refreshToken,
                TimeConverter.convertToMilliseconds(REFRESH_TOKEN_LIFE_TIME)));
        return  isDisabledAccessToken && isDisabledRefreshToken;
    }

    public void sendConfirmValidIp(String email, String ip){
        String verifyToken = tokenService.generateTempEmailToken(email,ip);
        String verifyUrl = String.join("",APP_BASEURL,"auth/verification?token=",verifyToken);
        emailService
                .sendEmail(new EmailRequest(EmailEnum.CONFIRM_IP.getSubject(),
                        String.join(" ",EmailEnum.CONFIRM_IP.getContent(), verifyUrl)
                        ,List.of(email)));
    }
    // -----------------------------Login logout end-------------------------------

    // -----------------------------Registration flow start-------------------------------
    public boolean register(RegisterRequest request, String ip){
        accountRepository
                .findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new AppExceptions(ErrorCode.USER_EXISTED);
                });
        Account newAccount = accountMapper.toAccount(request);
        Role userRole = roleRepository.findByNameIgnoreCase("USER")
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND));
        newAccount.setRoleId(userRole.getId());
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        accountRepository.save(newAccount);
        loggerRepository.save(Logs.builder()
                        .actionName("REGISTRATION")
                        .ip(ip)
                .build());

        sendVerifyEmail(newAccount.getEmail(), ip);
        return true;
    }

    public void sendVerifyEmail(String email, String ip){
        String verifyToken = tokenService.generateTempEmailToken(email, ip);
        String verifyUrl = String.join("",APP_BASEURL,"auth/verification?token=",verifyToken);
        emailService
                .sendEmail(new EmailRequest(EmailEnum.VERIFY_EMAIL.getSubject(),
                                String.join(" ",EmailEnum.VERIFY_EMAIL.getContent(), verifyUrl)
                                ,List.of(email)));
    }

    public Object verifyEmailAndIP(String token, String ip){
        Claims claims = tokenService.extractClaims(token);
        String email = claims.getSubject();
        String ipFromToken = claims.get("IP").toString();
        if(!tokenService.verifyToken(token) || !ip.equals(ipFromToken))
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);

        boolean foundLog =loggerRepository
                .existsByEmailAndIp(email, ipFromToken);

        Account account = getAccountByEmail(email);
        if(foundLog){
            account.setVerified(true);
            accountRepository.save(account);
            return true;
        }

        loggerRepository.save(Logs.builder()
                .actionName("CONFIRM_IP")
                .ip(ip)
                .build());

        return loginProcess(account, ip);
    }
    // -----------------------------Registration flow end-------------------------------

    // -----------------------------User information start-------------------------------
    // profile
    public UserResponse getProfile() {
        Account foundUser = getCurrentUser();
        return UserResponse.builder()
                .email(foundUser.getEmail())
                .address(foundUser.getAddress())
                .fullname(foundUser.getFullname())
                .gender(foundUser.getGender())
                .cloudImageUrl(foundUser.getCloudImageUrl())
                .build();
    }

    public boolean updateProfile(UpdateProfileRequest request, MultipartFile image) throws IOException {
        Account foundUser = getCurrentUser();
        if(request != null){
            accountMapper.updateAccount(foundUser, request);
        }
        if(request != null && request.getCloudImageUrl() == null && image != null) {
            String oldCloudId = foundUser.getCloudImageId();
            if(oldCloudId != null){
                cloudinaryService.delete(oldCloudId);
            }
            Map<?,?> cloudResponse = cloudinaryService.upload(image);
            CloudResponse cloudResponseDTO = cloudImageMapper.toCloudResponse(cloudResponse);
            foundUser.setCloudImageId(cloudResponseDTO.getPublicId());
            foundUser.setCloudImageUrl(cloudResponseDTO.getUrl());
        }
        accountRepository.save(foundUser);
        return true;
    }

    // password
    public boolean changePassword(ChangePasswordRequest request, String ip) {
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        if(currentPassword.equals(newPassword)) throw new AppExceptions(ErrorCode.PASSWORD_MUST_DIFFERENCE);

        Account foundUser = getCurrentUser();
        boolean isCorrectPassword = passwordEncoder.matches(request.getCurrentPassword(), foundUser.getPassword());
        if(!isCorrectPassword) throw new AppExceptions(ErrorCode.WRONG_PASSWORD);

        foundUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(foundUser);

        loggerRepository.save(Logs.builder()
                .actionName("CHANGE_PASSWORD")
                .ip(ip)
                .build());
        return true;
    }

    public boolean forgotPassword(String email, String ip) {
        getAccountByEmail(email);
        sendForgotPasswordEmail(email, ip);
        return true;
    }

    public boolean resetPassword(String token, String newPassword, String ip) {
        String email = tokenService.getTokenDecoded(token).getSubject();

        String key = String.join("","forgot-password-attempt:", email);
        String attempValueString = redisTemplate.opsForValue().get(key);

        String activeToken = attempValueString != null ? attempValueString.split("@")[1] : null;
        if(!tokenService.verifyToken(token) || email == null || !Objects.equals(activeToken, token))
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);

        Account foundAccount = getAccountByEmail(email);
        foundAccount.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(foundAccount);

        loggerRepository.save(Logs.builder()
                .actionName("RESET_PASSWORD")
                .ip(ip)
                .build());

        tokenService.deActiveToken(new Token(token, TimeConverter.convertToMilliseconds(EMAIL_TOKEN_LIFE_TIME)));

        sendResetPasswordSuccess(email);
        return true;
    }

    public void sendForgotPasswordEmail(String email, String ip){
        String forgotPasswordToken = tokenService.generateTempEmailToken(email, ip);

        String key = String.join("","forgot-password-attempt:", email);
        String attempValueString = redisTemplate.opsForValue().get(key);

        Integer attemptTime = attempValueString != null ? Integer.parseInt(attempValueString.split("@")[0])+1 : 1;

        if(attemptTime.equals(MAX_FORGOT_PASSWORD_ATTEMPT+1))
            throw new AppExceptions(ErrorCode.TOO_MUCH_FORGOT_PASSWORD_ATTEMPT);

        String value = String.join("@", attemptTime.toString(), forgotPasswordToken);

        redisTemplate.opsForValue().set(key, value,
                Duration.ofMillis(TimeConverter.convertToMilliseconds(DELAY_FORGOT_PASSWORD)));

        String verifyUrl = String.join("",APP_BASEURL,"auth/resetPassword?token=",forgotPasswordToken);
        emailService
                .sendEmail(new EmailRequest(EmailEnum.FORGOT_PASSWORD.getSubject(),
                        String.join(" ",EmailEnum.FORGOT_PASSWORD.getContent(), verifyUrl)
                        ,List.of(email)));
    }

    public void sendResetPasswordSuccess(String email){
        emailService
                .sendEmail(new EmailRequest(EmailEnum.RESET_PASSWORD_SUCCESS.getSubject(),
                        String.join(" ",EmailEnum.RESET_PASSWORD_SUCCESS.getContent(), LocalDateTime.now().toString())
                        ,List.of(email)));
    }
    // -----------------------------User information end-------------------------------

    // -----------------------------Utilities start-------------------------------
    public Account getAccountByEmail(String email){
        return accountRepository.findByEmail(email)
                .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
    }

    public Account getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymous")) throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        return getAccountByEmail(email);
    }

    public String getNewAccessToken(String refreshToken){
        if(!tokenService.verifyToken(refreshToken) ||
                Objects.isNull(tokenService.getTokenDecoded(refreshToken).getSubject())){
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        }

        String email = tokenService.getTokenDecoded(refreshToken).getSubject();
        Account foundAccount = getAccountByEmail(email);
        return tokenService.accessTokenFactory(foundAccount);
    }
    // -----------------------------Utilities end-------------------------------
}
