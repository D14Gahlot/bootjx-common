package com.boot.jx.logger;

import java.util.Map;

import com.boot.jx.dict.UserClient.UserDeviceClient;
import com.boot.jx.exception.AmxApiException;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ AuditEvent.PROP_DESC, AuditEvent.PROP_MSG, AbstractEvent.PROP_COMPONENT, AbstractEvent.PROP_CATG,
		AbstractEvent.PROP_TYPE, AuditEvent.PROP_RESULT, AbstractEvent.PROP_TIMSTAMP })
public abstract class AuditEvent<T extends AuditEvent<T>> extends AbstractEvent {

	public static final String PROP_MSG = "msg";
	public static final String PROP_DESC = "desc";
	public static final String PROP_RESULT = "rslt";
	private static final long serialVersionUID = -1539116953165424464L;

	@JsonProperty(PROP_RESULT)
	protected Result result;

	protected String errorCode;

	@JsonProperty("trxTym")
	protected long tranxTime;

	@JsonProperty("trcTym")
	protected long traceTime;

	@JsonProperty("evtTym")
	protected long eventTime;

	@JsonProperty(PROP_DESC)
	protected String description = null;

	@JsonProperty(PROP_MSG)
	protected String message;

	@JsonProperty("excp")
	protected String exception;

	@JsonProperty("excpTyp")
	protected String exceptionType;

	protected String actorId;

	protected Object data;
	protected UserDeviceClient client;

	@JsonIgnore
	boolean success;
	protected Map<String, String> details;

	public static enum Result {
		DEFAULT, DONE, REJECTED, FAIL, CANCELLED, ERROR, PASS, ALERT, TIMEOUT;
	}

	public AuditEvent() {
		super();
		this.result = Result.DONE;
	}

	public AuditEvent(EventType type, Result result) {
		super(type);
		this.result = result;
	}

	public AuditEvent(EventType type) {
		this(type, Result.DONE);
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDescription() {
		if (this.description == null) {
			return String.format("%s_%s", this.type, this.result);
		}
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getTranxTime() {
		return tranxTime;
	}

	public void setTranxTime(long tranxTime) {
		this.tranxTime = tranxTime;
	}

	public long getTraceTime() {
		return traceTime;
	}

	public void setTraceTime(long traceTime) {
		this.traceTime = traceTime;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public String getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(String exceptionType) {
		this.exceptionType = exceptionType;
	}

	/**
	 * gets the minimum actor info
	 * 
	 * @param actorId
	 */
	public String getActorId() {
		return actorId;
	}

	/**
	 * Sets the minimum actor info
	 * 
	 * @param actorId
	 */
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}

	public long getEventTime() {
		return eventTime;
	}

	public void setEventTime(long eventTime) {
		this.eventTime = eventTime;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public void clean() {

	}

	public UserDeviceClient getClient() {
		return client;
	}

	public void setClient(UserDeviceClient client) {
		this.client = client;
	}

	@SuppressWarnings("unchecked")
	public T result(Result result) {
		this.setResult(result);
		return (T) this;
	}

	public void setException(Exception excep) {
		this.setExceptionType(excep.getClass().getName());
		this.setException(excep.getMessage());
	}

	public void setException(AmxApiException excep) {
		this.setExceptionType(excep.getClass().getName());
		this.setException(excep.getMessage());
		this.errorCode = ArgUtil.isEmpty(excep.getErrorKey()) ? ArgUtil.parseAsString(excep.getError())
				: excep.getErrorKey();
	}

	@SuppressWarnings("unchecked")
	public T excep(Exception excep) {
		this.setException(excep);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T excep(AmxApiException excep) {
		this.setException(excep);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T result(Result result, AmxApiException excep) {
		this.setResult(result);
		this.setException(excep);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T message(Object message) {
		this.setMessage(ArgUtil.parseAsString(message));
		return (T) this;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Map<String, String> getDetails() {
		return details;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;
	}

}
