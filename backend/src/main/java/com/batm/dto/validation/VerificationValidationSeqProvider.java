package com.batm.dto.validation;

import com.batm.dto.UserVerificationDTO;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import java.util.ArrayList;
import java.util.List;

public class VerificationValidationSeqProvider implements DefaultGroupSequenceProvider<UserVerificationDTO> {

    @Override
    public List<Class<?>> getValidationGroups(UserVerificationDTO userVerificationDTO) {
        List<Class<?>> sequence = new ArrayList<>();
        if (userVerificationDTO != null) {
            if (userVerificationDTO.getTierId() != null) {
                if (userVerificationDTO.getTierId() == 1) {
                    sequence.add(BasicVerificationValidator.class);
                } else if (userVerificationDTO.getTierId() == 2) {
                    sequence.add(VIPVerificationValidator.class);
                }
            }
        }

        sequence.add(UserVerificationDTO.class);

        return sequence;
    }
}