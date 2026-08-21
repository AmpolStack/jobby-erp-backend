package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.CacheService;
import com.jobby.userservice.domain.models.vo.shared.Email;
import com.jobby.userservice.domain.ports.out.services.EmailChangeCodeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmailChangeCodeServiceAdapter implements EmailChangeCodeService {

    private final CacheService cacheService;
    private static final String PATH_KEY = "user.change-email.";
    private static final Duration EXPIRES_IN = Duration.ofMinutes(5);

    private static final int CODE_SIZE = 8;
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public Result<String, Error> next(long userId, Email newEmail) {
        var key = PATH_KEY + userId;
        var code = generateCode();
        var value = new ChangeCodeRequest(code, newEmail.getEmail());
        return this.cacheService.register(key, value, EXPIRES_IN)
                .map(v -> code);
    }

    @Override
    public Result<Email, Error> validate(long userId, String code) {
        var key = PATH_KEY + userId;
        return this.cacheService.get(key, ChangeCodeRequest.class)
                .flatMap(request -> {
                    if(request == null){
                        return Result.failure(ErrorType.NOT_FOUND, new Field("email change confirmation",
                                "There is no registered request with that ID"));
                    }
                    else if(!Objects.equals(request.code(), code)){
                        return Result.failure(ErrorType.VALIDATION_ERROR, new Field("email change code",
                                "The code is incorrect"));
                    }

                    return this.cacheService.remove(key)
                            .map(v -> Email.reconstruct(request.newEmail()));
                });
    }

    private static String generateCode(){
        SecureRandom random = new SecureRandom();
        return random.ints(CODE_SIZE, 0, CODE_CHARS.length())
                .mapToObj(i -> String.valueOf(CODE_CHARS.charAt(i)))
                .collect(Collectors.joining());
    }

    private record ChangeCodeRequest(String code, String newEmail){}
}
