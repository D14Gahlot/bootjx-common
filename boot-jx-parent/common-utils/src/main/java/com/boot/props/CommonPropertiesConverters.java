package com.boot.props;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.boot.utils.TimeUtils;
import com.boot.utils.TimeUtils.TimePeriod;

public class CommonPropertiesConverters {
    @Component
    @ConfigurationPropertiesBinding
    public class TimePeriodConverter implements Converter<String, TimePeriod> {

	@Override
	public TimePeriod convert(String from) {
	    TimePeriod period = new TimePeriod();
	    long millis = TimeUtils.toMillis(from);
	    period.setMillis(millis);
	    return period;
	}
    }
}
