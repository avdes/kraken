/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb;

import java.text.ParseException;
import java.util.Collection;

import org.krakenapps.bnf.Syntax;
import org.krakenapps.logdb.query.parser.QueryParser;

public interface SyntaxProvider {
	Syntax getSyntax();

	LogQueryCommand eval(LogQuery logQuery, String query) throws ParseException;

	void addParsers(Collection<QueryParser> parsers);

	void removeParsers(Collection<QueryParser> parsers);
}
