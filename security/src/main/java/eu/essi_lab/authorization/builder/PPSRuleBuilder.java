package eu.essi_lab.authorization.builder;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Condition;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.EffectType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Rule;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Target;

/**
 * @author Fabrizio
 */
public class PPSRuleBuilder {

    private ArrayList<AnyOf> anyOfList;
    private Condition condition;
    private String ruleIdentifier;

    /**
     * 
     */
    public PPSRuleBuilder(String ruleIdentifier) {

	this.ruleIdentifier = ruleIdentifier;
	this.anyOfList = new ArrayList<AnyOf>();
    }

    /**
     * @param anyOf
     * @return
     */
    public PPSRuleBuilder addTargetAnyOf(AnyOf anyOf) {

	anyOfList.add(anyOf);
	return this;
    }

    /**
     * @param condition
     * @return
     */
    public PPSRuleBuilder setCondition(Condition condition) {

	this.condition = condition;
	return this;
    }

    /**
     * @return
     */
    public Rule build() {

	Target target = new Target(anyOfList);

	return new Rule(//
		null, //
		target, //
		condition, //
		null, //
		null, //
		ruleIdentifier, //
		EffectType.PERMIT);

    }
}
