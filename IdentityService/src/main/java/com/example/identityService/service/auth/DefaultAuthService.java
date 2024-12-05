package com.example.identityService.service.auth;

import com.example.identityService.DTO.EmailEnum;
import com.example.identityService.DTO.EnumRole;
import com.example.identityService.DTO.request.ChangePasswordRequest;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.EmailRequest;
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
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.mapper.CloudImageMapper;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.LoggerRepository;
import com.example.identityService.service.AccountRoleService;
import com.example.identityService.service.CloudinaryService;
import com.example.identityService.service.EmailService;
import com.example.identityService.service.TokenService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultAuthService extends AbstractAuthService {

    @NonFinal
    @Value(value = "${app.baseUrl}")
    private String APP_BASEURL;

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
    @Value(value = "${security.authentication.jwt.email-token-life-time}")
    private String EMAIL_TOKEN_LIFE_TIME;

    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    private final CloudinaryService cloudinaryService;
    private final CloudImageMapper cloudImageMapper;
    private final EmailService emailService;
    private final LoggerRepository loggerRepository;


    private final RedisTemplate<String, String> redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final AccountMapper accountMapper;

    private final AccountRoleService accountRoleService;

    // -----------------------------Login logout start-------------------------------
    @Override
    public LoginResponse performLogin(LoginRequest request){
       return loginProcess(request.getEmail(), request.getIp());
    }

    @Override
    public LoginResponse performLoginWithGoogle(String email, String password, String ip){
        return loginProcess(email, ip);
    }

    @Override
    public boolean logout(String accessToken, String refreshToken) {
        boolean isDisabledAccessToken = tokenService.deActiveToken(new Token(accessToken,
                TimeConverter.convertToMilliseconds(ACCESS_TOKEN_LIFE_TIME)));
        boolean isDisabledRefreshToken = tokenService.deActiveToken(new Token(refreshToken,
                TimeConverter.convertToMilliseconds(REFRESH_TOKEN_LIFE_TIME)));
        return  isDisabledAccessToken && isDisabledRefreshToken;
    }

    public LoginResponse loginProcess(String email, String ip){
        Account foundAccount = accountRepository.findByEmail(email)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        String accessToken = tokenService.accessTokenFactory(foundAccount);
        String refreshToken = tokenService.generateRefreshToken(email, ip);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // -----------------------------Login logout end-------------------------------

    // -----------------------------Registration flow start-------------------------------

    @Override
    public boolean performRegister(RegisterRequest request) {
        accountRepository
                .findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new AppExceptions(ErrorCode.USER_EXISTED);
                });
        Account newAccount = accountMapper.toAccount(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        newAccount.setEnable(true);

        createAppUserAndAssignRole(newAccount, request.getIp());

        sendVerifyEmail(newAccount.getEmail(), request.getIp());
        return true;
    }

    @Override
    public boolean performCreateUser(CreateAccountRequest request) {
        accountRepository
                .findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new AppExceptions(ErrorCode.USER_EXISTED);
                });
        Account newAccount = accountMapper.toAccount(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        Account savedAccount = accountRepository.save(newAccount);
        return accountRoleService
                .assignRolesForUser(savedAccount.getId(), request.getRoles());
    }

    @Override
    public boolean performRegisterUserFromGoogle(Account request, String ip) {
        createAppUserAndAssignRole(request, ip);
        return true;
    }

    public Object verifyEmailAndIP(String token, String ip){
        Claims claims = tokenService.extractClaims(token);
        if(claims == null) throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        String email = claims.getSubject();
        String ipFromToken = claims.get("IP").toString();
        if(!tokenService.verifyToken(token) || !ip.equals(ipFromToken))
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);

        boolean foundLog = loggerRepository
                .existsByEmailAndIp(email, ipFromToken);

        Account account = getAccountByEmail(email);
        if(foundLog){
            account.setVerified(true);
            accountRepository.save(account);
            return true;
        }

        loggerRepository.save(Logs.builder()
                .actionName("CONFIRM_IP")
                .email(email)
                .ip(ip)
                .build());

        return loginProcess(account.getEmail(), ip);
    }
    // -----------------------------Registration flow end-------------------------------

    // -----------------------------User information start-------------------------------
    // profile
    public UserResponse getProfile(String token) {
        Account foundUser = getCurrentUser();
        return accountMapper.toUserResponse(foundUser);
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
    @Override
    public boolean performChangePassword(ChangePasswordRequest request, String ip) {
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        if(currentPassword.equals(newPassword)) throw new AppExceptions(ErrorCode.PASSWORD_MUST_DIFFERENCE);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Account foundUser = accountRepository.findByEmail(email)
                .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        boolean isCorrectPassword = passwordEncoder.matches(request.getCurrentPassword(), foundUser.getPassword());
        if(!isCorrectPassword) throw new AppExceptions(ErrorCode.WRONG_PASSWORD);

        foundUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(foundUser);

        loggerRepository.save(Logs.builder()
                .actionName("CHANGE_PASSWORD")
                .email(foundUser.getEmail())
                .ip(ip)
                .build());
        return true;
    }

    public boolean forgotPassword(String email, String ip) {
        getAccountByEmail(email);
        sendForgotPasswordEmail(email, ip);
        return true;
    }

    @Override
    public boolean performResetPassword(String token, String newPassword, String ip) {
        String email = tokenService.getTokenDecoded(token).getSubject();

        String key = String.join("","forgot-password-attempt:", email);
        String attempValueString = redisTemplate.opsForValue().get(key);

        String activeToken = attempValueString != null ? attempValueString.split("@")[1] : null;
        if(!tokenService.verifyToken(token) || email == null || !Objects.equals(activeToken, token))
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);

        tokenService.deActiveToken(new Token(token, TimeConverter.convertToMilliseconds(EMAIL_TOKEN_LIFE_TIME)));

        Account foundAccount = accountRepository.findByEmail(email)
                .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(foundAccount);

        loggerRepository.save(Logs.builder()
                .actionName("RESET_PASSWORD")
                .email(email)
                .ip(ip)
                .build());

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

    // -----------------------------User information end-------------------------------

    // -----------------------------Utilities start-------------------------------
    public Account getAccountByEmail(String email){
        return accountRepository.findByEmail(email)
                .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
    }

    public Account getCurrentUser(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if(email.equals("anonymousUser")) throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        return getAccountByEmail(email);
    }

    @Override
    public String getNewToken(String refreshToken){
        if(!tokenService.verifyToken(refreshToken) ||
                Objects.isNull(tokenService.getTokenDecoded(refreshToken).getSubject())){
            throw new AppExceptions(ErrorCode.UNAUTHENTICATED);
        }

        String email = tokenService.getTokenDecoded(refreshToken).getSubject();
        Account foundAccount = getAccountByEmail(email);
        return tokenService.accessTokenFactory(foundAccount);
    }


    public void createAppUserAndAssignRole(Account account, String ip){
        Account savedAccount = accountRepository.save(account);
        loggerRepository.save(Logs.builder()
                .actionName("REGISTRATION")
                .email(savedAccount.getEmail())
                .ip(ip)
                .build());
        accountRoleService.assignRolesForUser(savedAccount.getId(), List.of(EnumRole.USER.getName()));
    }
    // -----------------------------Utilities end-------------------------------
}
