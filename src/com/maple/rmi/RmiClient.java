package com.maple.rmi;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import lipermi.handler.CallHandler;
import lipermi.net.Client;

public class RmiClient {
	public static Document string2Doc(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document doc = null;
		InputSource source = null;
		StringReader reader = null;
		try {
			builder = factory.newDocumentBuilder();
			reader = new StringReader(xml);
			source = new InputSource(reader);// 使用字符流创建新的输入源
			doc = builder.parse(source);
			return doc;
		} catch (Exception e) {
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static void main(String[] args) {
		String content = "<sysmsg type=\"sysmsgtemplate\">\n" + "<sysmsgtemplate>\n"
				+ "<content_template type=\"tmpl_type_profile\">\n" + "<plain><![CDATA[]]></plain>\n"
				+ "<template><![CDATA[\"$username$\"邀请\"$names$\"加入了群聊]]></template>\n" + "<link_list>\n"
				+ "<link name=\"username\" type=\"link_profile\">\n" + "<memberlist>\n" + "<member>\n"
				+ "<username><![CDATA[wxid_z3wnioa1tncy11]]></username>\n" + "<nickname><![CDATA[刘武忠]]></nickname>\n"
				+ "</member>\n" + "</memberlist>\n" + "</link>\n" + "<link name=\"names\" type=\"link_profile\">\n"
				+ "<memberlist>\n" + "<member>\n" + "<username><![CDATA[MappleFeng]]></username>\n"
				+ "<nickname><![CDATA[机器人]]></nickname>\n" + "</member>\n" + "</memberlist>\n"
				+ "<separator><![CDATA[、]]></separator>\n" + "</link>\n" + "</link_list>\n" + "</content_template>\n"
				+ "</sysmsgtemplate>\n" + "</sysmsg>";
		Document document = string2Doc(content);

		String username = "";
		String nickname = "";
		String members = "";
		String memberName = "";

		NodeList list = document.getElementsByTagName("sysmsgtemplate");
		if (list.getLength() == 0)
			return;
		Element tmp = (Element) list.item(0);
		list = tmp.getElementsByTagName("content_template");
		if (list.getLength() == 0)
			return;
		tmp = (Element) list.item(0);
		list = tmp.getElementsByTagName("link_list");
		if (list.getLength() == 0)
			return;
		list = tmp.getElementsByTagName("link");
		for (int i = list.getLength(); --i >= 0;) {
			Element link = (Element) list.item(i);
			if (link.getAttribute("name").equals("username")) {
				tmp = (Element) link.getElementsByTagName("memberlist").item(0);
				tmp = (Element) link.getElementsByTagName("member").item(0);

				NodeList childNoList = tmp.getChildNodes();
				for (int j = 0; j < childNoList.getLength(); j++) {
					Node childNode = childNoList.item(j);
					// 判断子note类型是否为元素Note
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						Element childElement = (Element) childNode;
						if ("username".equals(childElement.getNodeName())) {
							username = childElement.getFirstChild().getNodeValue();
						} else if ("nickname".equals(childElement.getNodeName())) {
							nickname = childElement.getFirstChild().getNodeValue();
						}

					}
				}
			} else {
				tmp = (Element) link.getElementsByTagName("memberlist").item(0);
				NodeList memberlist = link.getElementsByTagName("member");
				for (int k = 0; k < memberlist.getLength(); k++) {
					Element n = (Element) memberlist.item(k);
					NodeList childNoList = n.getChildNodes();
					for (int j = 0; j < childNoList.getLength(); j++) {
						Node childNode = childNoList.item(j);
						// 判断子note类型是否为元素Note
						if (childNode.getNodeType() == Node.ELEMENT_NODE) {
							Element childElement = (Element) childNode;
							if ("username".equals(childElement.getNodeName())) {
								members += childElement.getFirstChild().getNodeValue() + "|";
							} else if ("nickname".equals(childElement.getNodeName())) {
								memberName += childElement.getFirstChild().getNodeValue() + "、";
							}

						}
					}
				}
				if(members.length() > 0) {
					members = members.substring(0, members.length() - 1);
				}
				if(memberName.length() > 0) {
					memberName = memberName.substring(0, memberName.length() - 1);
				}
			}
		}

		String wxId = "wxid_jznp0zl1v0ms22";
		String targetIds = "wxid_jznp0zl1v0ms22:wxid_z3wnioa1tncy11";
		if (targetIds.contains(wxId)) {
			String[] users = targetIds.split("\\|");
			for (String user : users) {
				String[] id = user.split(":");
				if (id.length > 1 && id[0].equals(wxId)) {

				}
			}
		}

		// 建立连接
		CallHandler callHandler = new CallHandler();
		String remoteHost = "192.168.31.88";
		int port = 8099;
		Client client = null;
		try {
			client = new Client(remoteHost, port, callHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 获取远程实例，测试方法
		assert client != null;
		LocalMessageInterface remoteObj = (LocalMessageInterface) client.getGlobal(LocalMessageInterface.class);
		System.out.println(remoteObj.sayHello("Tomcat"));
		System.out.println(remoteObj.getDate());
	}
}
