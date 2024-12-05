package com.example.identityService.service.auth;

import com.example.identityService.DTO.EmailEnum;
import com.example.identityService.DTO.EnumRole;
import com.example.identityService.DTO.Token;
import com.example.identityService.DTO.request.ChangePasswordRequest;
import com.example.identityService.DTO.request.EmailRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.response.GoogleUserResponse;
import com.example.identityService.DTO.response.LoginResponse;
import com.example.identityService.Util.RandomCodeCreator;
import com.example.identityService.Util.TimeConverter;
import com.example.identityService.config.KeycloakProvider;
import com.example.identityService.entity.Account;
import com.example.identityService.entity.Logs;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.LoggerRepository;
import com.example.identityService.service.AccountRoleService;
import com.example.identityService.service.EmailService;
import com.example.identityService.service.TokenService;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractAuthService{

    @Value(value = "${app.baseUrl}")
    private String APP_BASEURL;
    @Value(value = "${security.authentication.max-login-attempt}")
    private Integer MAX_LOGIN_ATTEMPT;
    @Value(value = "${security.authentication.login-delay-fail}")
    private String LOGIN_DELAY_FAIL;
    @Value(value = "${security.authentication.jwt.email-token-life-time}")
    private String EMAIL_TOKEN_LIFE_TIME;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private LoggerRepository loggerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private KeycloakProvider keycloakProvider;
    @Autowired
    private AccountRoleService accountRoleService;
    @Autowired
    private GoogleAuthService googleAuthService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public abstract LoginResponse performLogin(LoginRequest request);
    public abstract LoginResponse performLoginWithGoogle(String email, String password, String ip);
    public abstract boolean logout(String accessToken, String refreshToken);
    public abstract Object getNewToken(String refreshToken);
    public abstract boolean performResetPassword(String email, String newPassword);
    public abstract boolean performChangePassword(String email, String oldPassword, String newPassword);

    // google
    public LoginResponse loginWithGoogle(String code, String ip) {
        var token = googleAuthService.exchangeToken(code);
        var userResponse = googleAuthService.getUserInfo(token.getAccessToken());

        return accountRepository.findByEmail(userResponse.getEmail())
                .map(account -> performLoginWithGoogle(account.getEmail(), account.getPassword(), ip))
                .orElseGet(() -> {
                    Account newAccount = createAccountPattern(userResponse);

                    createKeycloakUser(RegisterRequest.builder()
                            .email(newAccount.getEmail())
                            .password(newAccount.getPassword())
                            .fullname(newAccount.getFullname())
                            .ip(ip)
                            .build());

                    createAppUserAndAssignRole(newAccount, ip);

                    return performLoginWithGoogle(newAccount.getEmail(), newAccount.getPassword(), ip);
                });
    }

    private Account createAccountPattern(GoogleUserResponse userResponse) {
        return Account.builder()
                .email(userResponse.getEmail())
                .password(passwordEncoder.encode(RandomCodeCreator.generateCode() + ""))
                .fullname(userResponse.getName())
                .cloudImageUrl(userResponse.getPicture())
                .verified(userResponse.isEmailVerified())
                .enable(true)
                .deleted(false)
                .build();
    }

    // password
    public boolean changePassword(ChangePasswordRequest request, String ip){
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
        return performChangePassword(email, request.getCurrentPassword(), request.getNewPassword());
    }

    public boolean resetPassword(String token, String newPassword, String ip){
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

        return performResetPassword(foundAccount.getEmail(), newPassword);
    }

    // login
    public LoginResponse login(LoginRequest request){
        Account foundAccount = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        boolean result = isValidUserStatus(foundAccount);
        if(result){
            Account account = accountRepository
                    .findByEmail(request.getEmail()).orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
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

            boolean isNewIp = !loggerRepository.existsByEmailAndIp(account.getEmail(), request.getIp());
            if(isNewIp){
                sendConfirmValidIp(account.getEmail(), request.getIp());
            }
            loggerRepository.save(Logs.builder()
                    .actionName("LOGIN")
                    .email(account.getEmail())
                    .ip(request.getIp())
                    .build());

            return performLogin(request);
        }
        return null;
    }

    // registration
    public boolean register(RegisterRequest request){
        return createAppUser(request) && createKeycloakUser(request);
    }

    public boolean createAppUser(RegisterRequest request){
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

    public void createAppUserAndAssignRole(Account newAccount, String ip){
        Account savedAccount = accountRepository.save(newAccount);
        loggerRepository.save(Logs.builder()
                .actionName("REGISTRATION")
                .email(newAccount.getEmail())
                .ip(ip)
                .build());
        accountRoleService.assignRolesForUser(savedAccount.getId(), List.of(EnumRole.USER.getName()));
    }

    public boolean createKeycloakUser(RegisterRequest request){
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFullname().split(" ")[0]);
        user.setLastName(request.getFullname().split(" ")[1]);
        user.setEnabled(true);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(request.getPassword());
        passwordCred.setTemporary(false);

        user.setCredentials(Collections.singletonList(passwordCred));

        keycloakProvider.getRealmResourceWithAdminPrivilege().users().create(user);
        keycloakProvider.getKeycloak().close();
        return true;
    }

    // utilities
    public static boolean isValidUserStatus(Account account){
        if(!account.isVerified()) throw new AppExceptions(ErrorCode.NOT_VERIFY_ACCOUNT);
        if(!account.isEnable()) throw new AppExceptions(ErrorCode.ACCOUNT_LOCKED);
        if(account.isDeleted()) throw new AppExceptions(ErrorCode.ACCOUNT_DELETED);
        return true;
    }

    public void sendVerifyEmail(String email, String ip){
        String verifyToken = tokenService.generateTempEmailToken(email, ip);
        String verifyUrl = String.join("",APP_BASEURL,"auth/verification?token=",verifyToken);
        emailService
                .sendEmail(new EmailRequest(EmailEnum.VERIFY_EMAIL.getSubject(),
                        String.join(" ",EmailEnum.VERIFY_EMAIL.getContent(), verifyUrl)
                        , List.of(email)));
    }

    public void sendConfirmValidIp(String email, String ip){
        String verifyToken = tokenService.generateTempEmailToken(email,ip);
        String verifyUrl = String.join("",APP_BASEURL,"auth/verification?token=",verifyToken);
        emailService
                .sendEmail(new EmailRequest(EmailEnum.CONFIRM_IP.getSubject(),
                        String.join(" ",EmailEnum.CONFIRM_IP.getContent(), verifyUrl)
                        ,List.of(email)));
    }

    public void sendResetPasswordSuccess(String email){
        emailService
                .sendEmail(new EmailRequest(EmailEnum.RESET_PASSWORD_SUCCESS.getSubject(),
                        String.join(" ",EmailEnum.RESET_PASSWORD_SUCCESS.getContent(), LocalDateTime.now().toString())
                        ,List.of(email)));
    }
}

