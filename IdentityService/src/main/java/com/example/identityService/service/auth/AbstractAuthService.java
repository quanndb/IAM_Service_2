package com.example.identityService.service.auth;

import com.example.identityService.DTO.EmailEnum;
import com.example.identityService.DTO.EnumRole;
import com.example.identityService.DTO.request.AppLogoutRequest;
import com.example.identityService.DTO.request.EmailRequest;
import com.example.identityService.DTO.request.LoginRequest;
import com.example.identityService.DTO.request.RegisterRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAuthService{

    @Value(value = "${app.baseUrl}")
    private String APP_BASEURL;
    @Value(value = "${security.authentication.max-login-attempt}")
    private Integer MAX_LOGIN_ATTEMPT;
    @Value(value = "${security.authentication.login-delay-fail}")
    private String LOGIN_DELAY_FAIL;

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
    private EmailService emailService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    public abstract Object performLogin(LoginRequest request);
    public abstract boolean logout(AppLogoutRequest request);
    public abstract boolean performRegister(RegisterRequest request);
    public abstract Object getNewToken(String refreshToken);

    public Object login(LoginRequest request){
        boolean result = checkUserStatus(request.getEmail());
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

    public boolean register(RegisterRequest request){
        return createAppUser(request) && createKeycloakUser(request) && performRegister(request);
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

        Account savedAccount = accountRepository.save(newAccount);
        loggerRepository.save(Logs.builder()
                .actionName("REGISTRATION")
                .email(newAccount.getEmail())
                .ip(request.getIp())
                .build());

        accountRoleService.assignRolesForUser(savedAccount.getId(), List.of(EnumRole.USER));

        sendVerifyEmail(newAccount.getEmail(), request.getIp());
        return true;
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

    public boolean checkUserStatus(String email){
        Account foundAccount = accountRepository
                .findByEmail(email).orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        if(!foundAccount.isVerified()) throw new AppExceptions(ErrorCode.NOT_VERIFY_ACCOUNT);
        if(!foundAccount.isEnable()) throw new AppExceptions(ErrorCode.ACCOUNT_LOCKED);
        if(foundAccount.isDeleted()) throw new AppExceptions(ErrorCode.ACCOUNT_DELETED);
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
}

