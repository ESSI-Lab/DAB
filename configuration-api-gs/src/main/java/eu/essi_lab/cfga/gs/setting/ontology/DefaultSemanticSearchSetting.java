/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.ontology;

import java.util.List;
import java.util.stream.Stream;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.utils.EuropeanLanguage;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class DefaultSemanticSearchSetting extends Setting {

    private static final String SOURCE_LANGUAGES_OPTION_KEY = "sourceLanguagesKey";
    private static final String SEARCH_LANGUAGES_OPTION_KEY = "searchLanguagesKey";
    private static final String SEM_RELATION_OPTION_KEY = "semRelationKey";
    private static final String EXP_LEVEL_OPTION_KEY = "expansionLevelKey";
    private static final String EXP_LIMIT_TARGET_OPTION_KEY = "expansionLimitTargetKey";
    private static final String EXP_LIMIT_OPTION_KEY = "expansionLimitValueKey";
    private static final String INCLUDE_ORIGINAL_SEARCH_TERM_KEY = "includeOriginalSTKey";

    /**
     * @param object
     */
    public DefaultSemanticSearchSetting(JSONObject object) {
	super(object);
    }

    /**
     * @param object
     */
    public DefaultSemanticSearchSetting(String object) {
	super(object);
    }

    /**
     * 
     */
    public DefaultSemanticSearchSetting() {

	setCanBeDisabled(false);
	setEditable(false);
	setEnabled(false);

	enableCompactMode(false);
	setName("Default semantic search setting");
	setDescription("Default settings evaluated by the semantic search engine");

	Option<SKOSSemanticRelation> semRelOption = OptionBuilder.get(SKOSSemanticRelation.class).//
		withKey(SEM_RELATION_OPTION_KEY).//
		withLabel("Default semantic relations").//
		withDescription("The default semantic relations by which expand the given term").//
		withMultiSelection().//
		withValues(LabeledEnum.values(SKOSSemanticRelation.class)).//
		withSelectedValues(List.of(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED)).//
		cannotBeDisabled().//
		build();

	addOption(semRelOption);

	Option<ExpansionLevel> expansionLevel = OptionBuilder.get(ExpansionLevel.class).//
		withKey(EXP_LEVEL_OPTION_KEY).//
		withLabel("Default expansion level").//
		withDescription("The default level by which expand the given term").//
		withSingleSelection().//
		withValues(LabeledEnum.values(ExpansionLevel.class)).//
		withSelectedValue(ExpansionLevel.LOW).//
		cannotBeDisabled().//
		build();

	addOption(expansionLevel);

	Option<LimitTarget> expansionLimitTarget = OptionBuilder.get(LimitTarget.class).//
		withKey(EXP_LIMIT_TARGET_OPTION_KEY).//
		withLabel("Default limitation target of the expansion").//
		withDescription(
			"The results of the semantic search expansion can be limited to a maximum number of concepts (default), or to a maximum number of labels or alternate labels.\n"
				+ "The default maximum number of results can set with the 'Default maximum number of results' option")
		.//
		withSingleSelection().//
		withValues(LabeledEnum.values(LimitTarget.class)).//
		withSelectedValue(LimitTarget.CONCEPTS).//
		cannotBeDisabled().//
		build();

	addOption(expansionLimitTarget);

	Option<Integer> expansionLimit = IntegerOptionBuilder.get().//
		withKey(EXP_LIMIT_OPTION_KEY).//
		withLabel("Default maximum number of results (0 = no limitation)").//
		withDescription(
			"The results of the semantic search expansion are limited by default to a maximum of 50.\n The value '0' means that no limitiation is applied.\n"
				+ "The default limitation target of the expansion can be set with the 'Default limitation target of the expansion' option")
		.//
		withSingleSelection().//
		withValues(Stream.iterate(0, n -> n + 1).limit(101).toList()).//
		withSelectedValue(50).//
		cannotBeDisabled().//
		build();

	addOption(expansionLimit);

	Option<EuropeanLanguage> srcLang = OptionBuilder.get(EuropeanLanguage.class).//
		withKey(SOURCE_LANGUAGES_OPTION_KEY).//
		withLabel("The default source languages").//
		withDescription("The default source languages in which the search term is expressed").//
		withMultiSelection().//
		withValues(LabeledEnum.values(EuropeanLanguage.class)).//
		withSelectedValues(List.of(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN)).//
		cannotBeDisabled().//
		build();

	addOption(srcLang);

	Option<EuropeanLanguage> searchLang = OptionBuilder.get(EuropeanLanguage.class).//
		withKey(SEARCH_LANGUAGES_OPTION_KEY).//
		withLabel("The default search languages").//
		withDescription("The default search languages in which to request the translation of each concept").//
		withMultiSelection().//
		withValues(LabeledEnum.values(EuropeanLanguage.class)).//
		withSelectedValues(List.of(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN)).//
		cannotBeDisabled().//
		build();

	addOption(searchLang);

	Option<BooleanChoice> includeOriginalOption = BooleanChoiceOptionBuilder.get().//
		withKey(INCLUDE_ORIGINAL_SEARCH_TERM_KEY).//
		withLabel("Include original search term")
		.withDescription(
			"If set to 'Yes' (default) the original search term is included in the search along with the terms outcome of the semantic expansion")
		.withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).cannotBeDisabled().//
		build();

	addOption(includeOriginalOption);
    }

    /**
     * @return
     */
    public ExpansionLimit getDefaultExpansionLimit() {

	Integer limit = getOption(EXP_LIMIT_OPTION_KEY, Integer.class).get().getSelectedValue();
	LimitTarget limitTarget = getOption(EXP_LIMIT_TARGET_OPTION_KEY, LimitTarget.class).get().getSelectedValue();

	return ExpansionLimit.of(limitTarget, limit);
    }

    /**
     * @return
     */
    public List<SKOSSemanticRelation> getDefaultSemanticRelations() {

	return getOption(SEM_RELATION_OPTION_KEY, SKOSSemanticRelation.class).get().getSelectedValues();
    }

    /**
     * @return
     */
    public ExpansionLevel getDefaultExpansionLevel() {

	return getOption(EXP_LEVEL_OPTION_KEY, ExpansionLevel.class).get().getSelectedValue();
    }

    /**
     * @return
     */
    public List<EuropeanLanguage> getDefaultSourceLanguages() {

	return getOption(SOURCE_LANGUAGES_OPTION_KEY, EuropeanLanguage.class).get().getSelectedValues();
    }

    /**
     * @return
     */
    public boolean isOriginalTermIncluded() {

	return BooleanChoice.toBoolean(getOption(INCLUDE_ORIGINAL_SEARCH_TERM_KEY, BooleanChoice.class).get().getSelectedValue());
    }

    /**
     * @return
     */
    public List<EuropeanLanguage> getDefaultSearchLanguages() {

	return getOption(SEARCH_LANGUAGES_OPTION_KEY, EuropeanLanguage.class).get().getSelectedValues();
    }

    /**
     * @return
     */
    public void setDefaultSemanticRelations(List<SKOSSemanticRelation> rel) {

	getOption(SEM_RELATION_OPTION_KEY, SKOSSemanticRelation.class).get().select(r -> rel.contains(r));
    }

    /**
     * @return
     */
    public void setDefaultExpansionLevel(ExpansionLevel level) {

	getOption(EXP_LEVEL_OPTION_KEY, ExpansionLevel.class).get().select(l -> l == level);
    }

    /**
     * @return
     */
    public void setDefaultSourceLanguages(List<EuropeanLanguage> lang) {

	getOption(SOURCE_LANGUAGES_OPTION_KEY, EuropeanLanguage.class).get().select(l -> lang.contains(l));
    }

    /**
     * @return
     */
    public void setDefaultSearchLanguages(List<EuropeanLanguage> lang) {

	getOption(SEARCH_LANGUAGES_OPTION_KEY, EuropeanLanguage.class).get().select(l -> lang.contains(l));
    }

    /**
     * @return
     */
    public void setDefaultExpansionLimit(ExpansionLimit limit) {

	getOption(EXP_LIMIT_OPTION_KEY, Integer.class).get().select(v -> v.equals(limit.getLimit()));
	getOption(EXP_LIMIT_TARGET_OPTION_KEY, LimitTarget.class).get().select(v -> v.equals(limit.getTarget()));
    }

    /**
     * @param include
     */
    public void setOriginalTermIncluded(boolean include) {

	getOption(INCLUDE_ORIGINAL_SEARCH_TERM_KEY, BooleanChoice.class).get().select(v -> BooleanChoice.toBoolean(v) == include);
    }
}
