package com.boot.jx.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.boot.jx.api.AmxResponseSchemes.ApiDataMetaResponse;
import com.boot.jx.api.AmxResponseSchemes.ApiResultsMetaResponse;
import com.boot.jx.exception.IExceptionEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AmxApiResponse<T, M> extends AResponse<M>
		implements ApiDataMetaResponse<T, M>, ApiResultsMetaResponse<T, M>, Serializable {

	private static final long serialVersionUID = 2026047322050489651L;

	/** The data. */
	protected T data = null;

	/** The data. */
	protected List<T> results = null;

	public AmxApiResponse() {
		super();
		this.data = null;
		this.results = new ArrayList<T>();
	}

	/**
	 * Instantiates a new amx api response.
	 *
	 * @param resultList the result list
	 */
	public AmxApiResponse(List<T> resultList) {
		super();
		this.data = null;
		this.results = resultList;
	}

	/**
	 * Instantiates a new amx api response.
	 *
	 * @param resultList the result list
	 * @param meta       the meta
	 */
	public AmxApiResponse(List<T> resultList, M meta) {
		super();
		this.data = null;
		this.results = resultList;
		this.meta = meta;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	@Override
	public T getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	@Override
	public void setData(T data) {
		this.data = data;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	@JsonIgnore
	public T getResult() {
		if (results != null && !results.isEmpty()) {
			return results.get(0);
		}
		return null;
	}

	@JsonIgnore
	public void setResult(T result) {
		this.results = new ArrayList<T>();
		this.results.add(result);
	}

	public void addResult(T result) {
		this.results.add(result);
	}

	public AmxApiResponse<T, M> result(T result) {
		this.addResult(result);
		return this;
	}

	public AmxApiResponse<T, M> results(List<T> resultList) {
		this.setResults(results);
		return this;
	}

	public static <TS, MS> AmxApiResponse<TS, MS> build() {
		return new AmxApiResponse<TS, MS>();
	}

	/**
	 * @deprecated - use {@link #buildResults(Object)} for list of elements and
	 *             {@link #buildResult(Object)} for single element list
	 * 
	 * @param <TS>
	 * @param result
	 * @return
	 */
	@Deprecated
	public static <TS> AmxApiResponse<TS, Object> build(TS result) {
		AmxApiResponse<TS, Object> resp = new AmxApiResponse<TS, Object>();
		resp.addResult(result);
		return resp;
	}

	/**
	 * @deprecated - use {@link #buildResults(Object, Object)} for list of elements
	 *             and {@link #buildResult(Object, Object)} for single element list
	 * 
	 * @param <TS>
	 * @param result
	 * @return
	 */
	@Deprecated
	public static <TS, MS> AmxApiResponse<TS, MS> build(TS result, MS meta) {
		return buildResult(result, meta);
	}

	/**
	 * Builds the list.
	 * 
	 * @deprecated - this method should not be used for list , use
	 *             {@link #buildResults(List)} for this
	 *
	 * @param <TS>       the generic type
	 * @param resultList the result list
	 * @return the amx api response
	 */
	@Deprecated
	public static <TS> AmxApiResponse<List<TS>, HashMap<String, Object>> build(List<TS> resultList) {
		return buildResult(resultList, new HashMap<String, Object>());
	}

	public static <MS> AmxApiResponse<Object, MS> buildMeta(MS meta) {
		AmxApiResponse<Object, MS> resp = new AmxApiResponse<Object, MS>();
		resp.setMeta(meta);
		return resp;
	}

	public static <TS> AmxApiResponse<TS, Object> buildData(TS data) {
		AmxApiResponse<TS, Object> resp = new AmxApiResponse<TS, Object>();
		resp.setData(data);
		return resp;
	}

	public static <TS, MS> AmxApiResponse<TS, MS> buildData(TS data, MS meta) {
		AmxApiResponse<TS, MS> resp = new AmxApiResponse<TS, MS>();
		resp.setData(data);
		resp.setMeta(meta);
		return resp;
	}

	/**
	 * Builds the list.
	 *
	 * @param <TS>       the generic type
	 * @param resultList the result list
	 * @return the amx api response
	 */
	public static <TS> AmxApiResponse<TS, Object> buildList(List<TS> resultList) {
		return buildList(resultList, new HashMap<String, Object>());
	}

	/**
	 * Builds the list.
	 *
	 * @param <TS>       the generic type
	 * @param <MS>       the generic type
	 * @param resultList the result list
	 * @param meta       the meta
	 * @return the amx api response
	 */
	public static <TS, MS> AmxApiResponse<TS, MS> buildList(List<TS> resultList, MS meta) {
		return buildResults(resultList, meta);
	}

	public static <TS> AmxApiResponse<TS, Object> buildResults(TS resultList) {
		return buildResult(resultList, new HashMap<String, Object>());
	}

	public static <TS, MS> AmxApiResponse<TS, MS> buildResults(TS resultList, MS meta) {
		return buildResult(resultList, meta);
	}

	public static <TS> AmxApiResponse<TS, Object> buildResults(List<TS> resultList) {
		return buildResults(resultList, new HashMap<String, Object>());
	}

	public static <TS, MS> AmxApiResponse<TS, MS> buildResults(List<TS> resultList, MS meta) {
		AmxApiResponse<TS, MS> resp = new AmxApiResponse<TS, MS>();
		// ArrayList<TS> listOfStrings = new ArrayList<TS>(resultList.size());
		// listOfStrings.addAll(resultList);
		resp.setResults(resultList);
		resp.setMeta(meta);
		return resp;
	}

	public static <TS, MS> AmxApiResponse<TS, MS> buildResult(TS result, MS meta) {
		AmxApiResponse<TS, MS> resp = new AmxApiResponse<TS, MS>();
		resp.addResult(result);
		resp.setMeta(meta);
		return resp;
	}

	public static <TS, MS> AmxApiResponse<TS, MS> buildResult(TS result) {
		AmxApiResponse<TS, MS> resp = new AmxApiResponse<TS, MS>();
		resp.addResult(result);
		return resp;
	}

	@JsonIgnore
	public AmxApiResponse<T, M> redirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
		return this;
	}

	public AmxApiResponse<T, M> statusKey(String status) {
		this.setStatus(status);
		return this;
	}

	public AmxApiResponse<T, M> statusEnum(IExceptionEnum statusEnum) {
		this.setStatusEnum(statusEnum);
		return this;
	}

	public AmxApiResponse<T, M> message(String message) {
		this.message = message;
		return this;
	}

	

}
