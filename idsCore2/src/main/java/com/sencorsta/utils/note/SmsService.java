package com.sencorsta.utils.note;/*
package com.sencorsta.utils.note;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.qiangu.GConfig;
import com.qiangu.logfs.Out;

public class SmsService {

	private static String SIGN = GConfig.getInstance().get("sms.name", "海上商人");

	private static String AccessKeyId = GConfig.getInstance().get("sms.id", "LTAIJHU0mOK7JASs");
	private static String AccessKeySecret = GConfig.getInstance().get("sms.secret", "vRMtucbAnEJysjlydnKsGdS1Ka98SH");
	private static String TemplateSend = GConfig.getInstance().get("sms.code", "SMS_130923981");
	private static String TemplateReset = GConfig.getInstance().get("sms.code", "SMS_130928966");

	// AssumeRole API 请求参数: RoleArn, RoleSessionName, Policy, and DurationSeconds
	// RoleArn 需要在 RAM 控制台上获取
//	private static String roleArn = "acs:ram::1460302104277586:role/abgame";
//	private static String roleSessionName = "alice-001";
//	// 如何定制你的policy?
//	private static String policy = "{\n" + "    \"Version\": \"1\", \n" + "    \"Statement\": [\n" + "        {\n"
//			+ "            \"Action\": [\n" + "                \"oss:GetBucket\", \n"
//			+ "                \"oss:GetObject\" \n" + "            ], \n" + "            \"Resource\": [\n"
//			+ "                \"acs:oss:*:*:*\"\n" + "            ], \n" + "            \"Effect\": \"Allow\"\n"
//			+ "        }\n" + "    ]\n" + "}";

//	private static String API_URL = "http://gw.api.taobao.com/router/rest";


	public static void sendSmsCode(String phone, String code) {
		String json = "{\"code\":\"" + code + "\"}";
		sendSms(phone, json);
	}

	public static void sendSmsCodeReset(String phone, String code) {
		String json = "{\"code\":\"" + code + "\"}";
		sendSmsReset(phone, json);
	}

	private static void sendSms(String phone, String json) {
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
		// 初始化ascClient需要的几个参数
		final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
		final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
		// 替换成你的AK
		// 初始化ascClient,暂时不支持多region
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", AccessKeyId, AccessKeySecret);
		SmsRequest request = new SmsRequest();
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			// 组装请求对象

			// 使用post提交
			request.setMethod(MethodType.POST);
			// 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
			request.setPhoneNumbers(phone);
			// 必填:短信签名-可在短信控制台中找到
			request.setSignName(SIGN);
			// 必填:短信模板-可在短信控制台中找到
			request.setTemplateCode(TemplateSend);
			// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
			// 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
			request.setTemplateParam(json);
			// 可选-上行短信扩展码(无特殊需求用户请忽略此字段)
			// request.setSmsUpExtendCode("90997");
			// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
			// 请求失败这里会抛ClientException异常
			SmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			System.out.println(sendSmsResponse.getCode());
			System.out.println(sendSmsResponse.getMessage());

			if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
				System.out.println(sendSmsResponse.getCode());
				System.out.println(sendSmsResponse.getMessage());
				Out.info("手机={0} ，发送成功", phone);

				// 请求成功
			}
		} catch (ClientException e) {
			Out.error("发送短信失败", e.getCause());
			throw new RuntimeException("短信发送失败");
		}
	}

	private static void sendSmsReset(String phone, String json) {
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
		// 初始化ascClient需要的几个参数
		final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
		final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
		// 替换成你的AK
		// 初始化ascClient,暂时不支持多region
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", AccessKeyId, AccessKeySecret);
		SmsRequest request = new SmsRequest();
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			// 组装请求对象

			// 使用post提交
			request.setMethod(MethodType.POST);
			// 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
			request.setPhoneNumbers(phone);
			// 必填:短信签名-可在短信控制台中找到
			request.setSignName(SIGN);
			// 必填:短信模板-可在短信控制台中找到
			request.setTemplateCode(TemplateReset);
			// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
			// 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
			request.setTemplateParam(json);
			// 可选-上行短信扩展码(无特殊需求用户请忽略此字段)
			// request.setSmsUpExtendCode("90997");
			// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
			// 请求失败这里会抛ClientException异常
			SmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			System.out.println(sendSmsResponse.getCode());
			System.out.println(sendSmsResponse.getMessage());

			if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
				System.out.println(sendSmsResponse.getCode());
				System.out.println(sendSmsResponse.getMessage());
				Out.info("手机={0} ，发送成功", phone);

				// 请求成功
			}
		} catch (ClientException e) {
			Out.error("发送短信失败", e.getCause());
			throw new RuntimeException("短信发送失败");
		}
	}
}
*/
