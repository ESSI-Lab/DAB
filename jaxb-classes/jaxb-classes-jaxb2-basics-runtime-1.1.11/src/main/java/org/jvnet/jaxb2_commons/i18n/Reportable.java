package org.jvnet.jaxb2_commons.i18n;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ResourceBundle;

/**
 * Reportable object.
 *
 * @author Aleksei Valikov
 */
public interface Reportable
{
  /**
   * Returns message code. This code will be used to locate message resource.
   *
   * @return String code that uniquely identifies this problem. May be used to reference messages.
   */
  public String getMessageCode();

  /**
   * Returns parameters used to format the message.
   *
   * @return Array of parameters used to format problem message.
   */
  public abstract Object[] getMessageParameters();

  /**
   * Formats the message using given resource bundle.
   *
   * @param bundle bundle to use resources from.
   * @return Formatted message.
   */
  public String getMessage(final ResourceBundle bundle);

  /**
   * Returns the message.
   *
   * @return The message.
   */
  public String getMessage();
}
