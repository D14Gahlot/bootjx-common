package com.boot.jx.postman.plugin;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeanUtils;

import com.boot.jx.AppContextUtil;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.common.impl.ConfigMeta.CONVERT_TYPE;
import com.boot.jx.common.impl.ConfigMeta.ConfigMetaProperty;
import com.boot.jx.common.impl.ConfigMeta.DATA_TYPE;
import com.boot.jx.common.impl.ConfigMeta.INPUT_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.ChannelTypeSpecificProps;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

public class ChannelPluginProvider {

	public static interface ChannelPlugin<C extends AChannelDetails> extends ChannelTypeSpecificProps {
		/**
		 * usually return new AChannelDetails();
		 * 
		 * @return new instance of {@link AChannelDetails}
		 */
		public C newChannelDetails();

		@SuppressWarnings("unchecked")
		default public C getChannelDetails(AChannelDetails channelDetails) {
			return (C) channelDetails;
		}

		/**
		 * For new Configs
		 * 
		 * @param config
		 * @param details
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public default ChannelConfig updateChannelConfig(ChannelConfig config, AChannelDetails details) {
			updatePluginSpecs(config);
			// Channel Specific Properties
			config.setLane(details.getLane());

			setDetails(config, (C) details);
			return config;
		}

		public default void updatePluginSpecs(ChannelConfig config) {
			// Plugin Specific Properties
			config.setContactType(this.getContactType());
			config.setChannelType(this.getChannelType());

			config.setPushAllowed(this.isPushAllowed());
			config.setPushOnlyApproved(this.isPushOnlyApproved());
			config.setPushFreeTextAllowed(this.isPushFreeTextAllowed());
			config.setPushToNewContactAllowed(this.isPushToNewContactAllowed());
			config.setWebhookManual(this.isWebhookManual());
		}

		/**
		 * For new Configs
		 * 
		 * @param config
		 * @param details
		 */
		public void setDetails(ChannelConfig config, C details);

		public C getDetails(ChannelConfig config);

		public void addConfigMeta(List<ConfigMeta> configMetaList);

		default public List<ConfigMeta> listConfigMeta(PMEnvironment pmEnvironment) {
			List<ConfigMeta> list = ConfigMeta.createList();
			list.add(new ConfigMeta().key("name").title("Desc"));
			list.add(new ConfigMeta().key("channelCode").title("Channel Code").max(4));
			list.add(new ConfigMeta().key("channelKey").title("Channel Key").readonly().hidden()
					.defaultValue(PostManUtil.UNIQUE_API_KEY()));
			list.add(new ConfigMeta().key("inboundQueue").title("Default Queue").optional()
					.optionsSource("getx:/api/options/inbound_queue").optionsKey("code").order(100));

			String serviceDomain = pmEnvironment.keyEntry("mry.prop.service.domain").asString();
			String clientDomain = AppContextUtil.getTenant();
			list.add(new ConfigMeta().key("webhookUrl").title("Webhook URL").hidden()
					.defaultValue(String.format("https://%s.%s/postman", clientDomain, serviceDomain)));

			this.addConfigMeta(list);
			return list;
		}

		void importChannelDetailsFromMap(C channelDetails, MapModel map);

		@SuppressWarnings("unchecked")
		default void importChannelDetailsFromMap(AChannelDetails channelDetails, MapModel map, String channelType) {
			importChannelDetailsFromMap((C) channelDetails, map);
		}

		default public void importChannelConfigFromMap(ChannelConfig config, MapModel map, String channelType) {
			AChannelDetails channelDetails = getDetails(config);
			if (channelDetails == null) {
				channelDetails = newChannelDetails();
			}
			importChannelDetailsFromMap(channelDetails, map, channelType);

			config.setName(map.getString("name", ArgUtil.nonEmpty(config.getName(), getDefaultName(config))));
			config.setChannelKey(map.getString("channelKey",
					ArgUtil.nonEmpty(config.getChannelKey(), PostManUtil.UNIQUE_API_KEY())));
			config.setChannelCode(map.getString("channelCode", config.getChannelCode()));

			config.setInboundQueue(map.getString("inboundQueue"));

			config.setWebhookUrl(map.getString("webhookUrl", config.getWebhookUrl()));
			updateChannelConfig(config, channelDetails);
		}

		default public String getDefaultName(ChannelConfig config) {
			if (!ArgUtil.is(config.getName())) {
				return String.format("%s %s", this.getContactType(), config.getLane());
			}
			return config.getName();
		}

	}

	public interface DefaultChannelPlugin<T extends AChannelDetails> extends ChannelPlugin<T> {

		@Override
		default public void addConfigMeta(List<ConfigMeta> configMetaList) {
			Class<?> clazz = AopProxyUtils.ultimateTargetClass(newChannelDetails());
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigMetaProperty.class)) {
					ConfigMetaProperty annotation = field.getAnnotation(ConfigMetaProperty.class);
					ConfigMeta cm = new ConfigMeta().path(annotation.path()).title(annotation.title())
							.createonly(annotation.createonly()).writeonly(annotation.writeonly());
					if (annotation.inputType() == INPUT_TYPE.OPTIONS && annotation.dataType() == DATA_TYPE.SWITCH
							&& annotation.converterType() == CONVERT_TYPE.BOOLEAN) {
						cm.optionsOnOff();
					} else if (annotation.inputType() == INPUT_TYPE.OPTIONS && ArgUtil.is(annotation.optionsSource())) {
						cm.optionsSource(annotation.optionsSource()).optionsKey(annotation.optionsKey())
								.optionsLabel(annotation.optionsLabel());
					}
					if (ArgUtil.is(annotation.defaultValue())) {
						cm.defaultValue(annotation.defaultValue());
					}
					configMetaList.add(cm);
				}
			}
		}

		@Override
		default public void importChannelDetailsFromMap(T channelDetails, MapModel map) {
			Class<?> clazz = AopProxyUtils.ultimateTargetClass(channelDetails);
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(ConfigMetaProperty.class)) {
					ConfigMetaProperty annotation = field.getAnnotation(ConfigMetaProperty.class);
					// pd = new PropertyDescriptor(field.getName(), clazz);
					PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, field.getName());
					if (ArgUtil.is(pd)) {
						Method setter = pd.getWriteMethod();
						Method getter = pd.getReadMethod();
						Type type = field.getGenericType();
						String typeName = type.getTypeName();
						try {
							Object currentValue = getter.invoke(channelDetails);
							if ("java.lang.String".equals(typeName)) {
								setter.invoke(channelDetails,
										map.pathEntry(annotation.path()).asString(ArgUtil.parseAsString(currentValue)));
							} else if ("int".equals(typeName) || "java.lang.Integer".equals(typeName)) {
								setter.invoke(channelDetails, map.pathEntry(annotation.path())
										.asInteger(ArgUtil.parseAsInteger(currentValue)));
							} else if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
								setter.invoke(channelDetails, map.pathEntry(annotation.path())
										.asBoolean(ArgUtil.parseAsBoolean(currentValue)));
							} else if ("java.lang.String[]".equals(typeName)) {
								setter.invoke(channelDetails, map.pathEntry(annotation.path()).value());
							} else if (type instanceof Class && ((Class<?>) type).isEnum()) {
								setter.invoke(channelDetails, map.pathEntry(annotation.path())
										.asEnum(ArgUtil.parseAsEnum(currentValue, type), type));
							} else {
								setter.invoke(channelDetails,
										map.pathEntry(annotation.path()).defaultValue(currentValue));
							}
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static final Map<String, ChannelPlugin<? extends AChannelDetails>> PLUGIN_MAPPING = new HashMap<String, ChannelPlugin<? extends AChannelDetails>>();
	public static final Map<String, AChannelDetails> DETAILS_MAPPING = new HashMap<String, AChannelDetails>();

	public static <C extends AChannelDetails> void register(ChannelPlugin<C> channelPlugin) {
		C details = channelPlugin.newChannelDetails();
		DETAILS_MAPPING.put(channelPlugin.getChannelType(), channelPlugin.newChannelDetails());
		PLUGIN_MAPPING.put(channelPlugin.getChannelType(), channelPlugin);
	}

	public ChannelPlugin<? extends AChannelDetails> get(String channelType) {
		return PLUGIN_MAPPING.getOrDefault(channelType, WEB);
	}

	public static final WebPlugin WEB = new WebPlugin();
	public static final FacebookPlugin FACEBOOK = new FacebookPlugin();
	public static final TwitterPlugin TWITTER = new TwitterPlugin();
	public static final TelegramPlugin TELEGRAM = new TelegramPlugin();
	public static final WAGupShupPlugin WA_GUPSHUP = new WAGupShupPlugin();
	public static final WA360Plugin WA_360D = new WA360Plugin();
	public static final InstagramPlugin INSTAGRAM = new InstagramPlugin();
	public static final EmailPlugin EMAIL = new EmailPlugin();

	static {
		register(WEB);
		register(FACEBOOK);
		register(TWITTER);
		register(TELEGRAM);
		register(WA_GUPSHUP);
		register(WA_360D);
		register(INSTAGRAM);
		register(EMAIL);
	}

}
