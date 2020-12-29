package com.boot.jx.rest;

public interface IMetaRequestOutFilter<T extends ARequestMetaInfo> {

	/**
	 * Meta Data Info you want to send with outgoing request
	 * 
	 * @param meta
	 */
	public void outFilter(T requestMeta);

}
