package com.jobby.infrastructure.adapter;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.IdGenerator;
import com.jobby.infrastructure.configurations.IdConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnowflakeIdGenerator implements IdGenerator {

    private final Snowflake snowflake;
    private final IdConfig idConfig;

    public SnowflakeIdGenerator(IdConfig config) {
        this.snowflake = IdUtil.getSnowflake(config.getWorkerId(), config.getDatacenterId());
        this.idConfig = config;
    }

    @Override
    public Result<Long, Error> next() {
        try{
            var id = this.snowflake.nextId();
            return Result.success(id);
        }
        catch (Exception ex){
            log.error("[ITS_INVALID_STATE] Snowflake ID generation failed: workerId={}, datacenterId={}", idConfig.getWorkerId(), idConfig.getDatacenterId(), ex);
            return Result.failure(ErrorType.ITS_INVALID_STATE, new Field("snowflake id",
                    ex.getClass().getSimpleName() + ": Snowflake generator failed"));
        }
    }
}
