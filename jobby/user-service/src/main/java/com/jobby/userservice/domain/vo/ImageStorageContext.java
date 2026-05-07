package com.jobby.userservice.domain.vo;

import lombok.AllArgsConstructor;
import java.util.UUID;

public abstract class ImageStorageContext {

    public abstract String getStorageRoute();
    public abstract String getFilename();

    @AllArgsConstructor
    public static class OwnerContext extends ImageStorageContext {
        private final long userId;

        @Override
        public String getStorageRoute() {
            return String.format("owner/%d/", userId);
        }

        @Override
        public String getFilename() {
            return UUID.randomUUID().toString();
        }
    }

    @AllArgsConstructor
    public static class EmployeeContext extends ImageStorageContext {
        private final long organizationId;
        private final long sectionalId;
        private final long employeeId;

        @Override
        public String getStorageRoute() {
            return String.format("org/%d/%d/employee/%d/", organizationId, sectionalId, employeeId);
        }

        @Override
        public String getFilename() {
            return UUID.randomUUID().toString();
        }
    }
}
