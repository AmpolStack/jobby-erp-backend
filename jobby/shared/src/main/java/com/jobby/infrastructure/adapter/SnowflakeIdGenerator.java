package com.jobby.infrastructure.adapter;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.jobby.infrastructure.configurations.IdConfig;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class SnowflakeIdGenerator implements IdGenerator {
    private final Snowflake snowflake;

    public SnowflakeIdGenerator(IdConfig config) {
        this.snowflake = IdUtil.getSnowflake(config.getWorkerId(), config.getDatacenterId());
    }

    @Override
    public Result<Long, Error> next() {
        try{
            var id = this.snowflake.nextId();
            return Result.success(id);
        }
        catch (Exception ex){
            return Result.failure(ErrorType.ITS_INVALID_STATE, new Field("snowflake id",
                    "An error occurred in the configuration or time parameters of the SnowflakeGenerator at: " + Instant.now().toString()));
        }
    }
}
