/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.cfg.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.xml.JDomDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.SubscribedPlan;

import java.util.Collection;
import java.util.ArrayList;

public final class JDomXStreamUtil {
	private static final String PLAN = "plan";
	private static final String PLAN_KEY = "key";

	private JDomXStreamUtil() {
	}

	public static XStream getProjectJDomXStream() {
		XStream xStream = new XStream(new JDomDriver());
		xStream.alias("bamboo", BambooServerCfg.class);
		xStream.alias("crucible", CrucibleServerCfg.class);
		xStream.alias("jira", JiraServerCfg.class);

		xStream.alias(PLAN, SubscribedPlan.class);
		xStream.omitField(ServerCfg.class, "username");
		xStream.omitField(ServerCfg.class, "password");
		xStream.omitField(ServerCfg.class, "isPasswordStored");
		xStream.aliasField("server-id", ServerCfg.class, "serverId");
		xStream.aliasField("enabled", ServerCfg.class, "isEnabled");
//		xStream.aliasField("password-stored", ServerCfg.class, "isPasswordStored");
		xStream.aliasField("use-favourites", ServerCfg.class, "isUseFavourites");
		xStream.aliasField("bamboo2", ServerCfg.class, "isBamboo2");

		
		xStream.alias("project-configuration", ProjectConfiguration.class);

		xStream.alias("private-server-cfg", PrivateServerCfgInfo.class);
		xStream.aliasField("server-id", PrivateServerCfgInfo.class, "serverId");
		xStream.registerLocalConverter(PrivateServerCfgInfo.class, "password", new EncodedStringConverter());

		xStream.alias("private-project-cfg", PrivateProjectConfiguration.class);
		xStream.aliasField("private-server-cfgs", PrivateProjectConfiguration.class, "privateServerCfgInfos");
		xStream.addDefaultImplementation(ArrayList.class, Collection.class);
		xStream.aliasField("plans", BambooServerCfg.class, "plans");
		xStream.registerLocalConverter(ServerCfg.class, "password", new EncodedStringConverter());
//		xStream.registerLocalConverter(BambooServerCfg.class, "plans", new Converter() {
//			public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
//				final Collection<?> plans = (Collection<?>) source;
//				for (Object o : plans) {
//					if (o instanceof SubscribedPlan) {
//						final SubscribedPlan plan = (SubscribedPlan) o;
//						writer.startNode(PLAN);
//						context.convertAnother(plan);
//						writer.endNode();
//					}
//				}
//			}
//
//			public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
//				Collection<SubscribedPlan> res = MiscUtil.buildArrayList();
//				while (reader.hasMoreChildren()) {
//					reader.moveDown();
//					if (PLAN.equals(reader.getNodeName())) {
//						SubscribedPlan plan = (SubscribedPlan) context.convertAnother(res, SubscribedPlan.class);
//						res.add(plan);
//					}
//					reader.moveUp();
//				}
//				return res;
//			}
//
//			public boolean canConvert(final Class aClass) {
//				return Collection.class.isAssignableFrom(aClass);
//			}
//		});
		xStream.registerConverter(new Converter() {

			public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
				SubscribedPlan value = (SubscribedPlan) source;
				writer.addAttribute(PLAN_KEY, value.getPlanId());
				//writer.setValue(value.getPlanId());
			}

			public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
				return new SubscribedPlan(reader.getAttribute(PLAN_KEY));
			}

			public boolean canConvert(final Class aClass) {
				return SubscribedPlan.class.isAssignableFrom(aClass);
			}
		});
		xStream.registerConverter(new Converter() {

			public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
				ServerId value = (ServerId) source;
				writer.setValue(value.getUuid().toString());
			}

			public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
				return new ServerId(reader.getValue());
			}

			public boolean canConvert(final Class type) {
				return type.equals(ServerId.class);
			}
		});
		return xStream;

	}


}
