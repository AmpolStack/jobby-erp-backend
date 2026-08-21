package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.domain.ports.in.CommandHandler;
import com.jobby.userservice.application.contracts.commands.UpdateUserCommand;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.application.responses.UserResponse;
import com.jobby.userservice.domain.models.aggregate.User;
import com.jobby.userservice.domain.models.vo.shared.IdentificationNumber;
import com.jobby.userservice.domain.models.vo.shared.Name;
import com.jobby.userservice.domain.models.vo.shared.Phone;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import com.jobby.userservice.domain.ports.out.services.ReferenceDataProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateUserUseCase implements CommandHandler<UpdateUserCommand, UserResponse> {

    private final UserRepository userRepository;
    private final ReferenceDataProvider referenceData;
    private final ResponseMapper responseMapper;
    private final TransactionOrchestrator transaction;

    @Override
    public Result<UserResponse, Error> execute(UpdateUserCommand command) {
        return this.referenceData.identificationType(command.identificationTypeId())
                .flatMap(identificationType -> {
                    var newFirstName = Name.of(command.firstName(), "firstName");
                    var newLastName = Name.of(command.lastName(), "lastName");
                    var newIdNumber = IdentificationNumber.of(command.identificationNumber(), identificationType);
                    var newPhone = Phone.of(command.phone());

                    return ValidationChain.create()
                            .add(newFirstName)
                            .add(newLastName)
                            .add(newIdNumber)
                            .add(newPhone)
                            .build()
                            .flatMap(v -> this.userRepository.getById(command.userId())
                                    .flatMap(user -> validateUniqueness(user, newIdNumber.data(), newPhone.data())
                                            .flatMap(v2 -> user.update(newFirstName.data(), newLastName.data(),
                                                    newIdNumber.data(), command.identificationTypeId(), newPhone.data()))
                                            .flatMap(v2 -> this.userRepository.prepareSave(user))
                                            .flatMap(task -> this.transaction.write().add(task).build())
                                            .map(v2 -> this.responseMapper.toResponse(user))));
                });
    }

    private Result<Void, Error> validateUniqueness(User user,
                                                   IdentificationNumber identificationNumber,
                                                   Phone phone) {
        var existIdNumber = user.getIdentificationNumber().equals(identificationNumber)
                ? Result.<Boolean, Error>success(false)
                : this.userRepository.existByIdentificationNumber(identificationNumber.getNumber());

        var existPhone = user.getPhone().equals(phone)
                ? Result.<Boolean, Error>success(false)
                : this.userRepository.existByPhone(phone.getNumber());

        return existIdNumber
                .flatMap(idExists -> idExists
                        ? Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("identificationNumber", "A user with that identification number is already registered."))
                        : existPhone)
                .flatMap(pExists -> pExists
                        ? Result.failure(ErrorType.VALIDATION_ERROR,
                        new Field("phone", "There is already a registered user with that phone number."))
                        : Result.success());
    }
}
