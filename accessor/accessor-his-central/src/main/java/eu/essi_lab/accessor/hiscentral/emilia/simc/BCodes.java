package eu.essi_lab.accessor.hiscentral.emilia.simc;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lookup for BUFR B-codes from dballe.txt (fixed-width: 6-digit code, description, unit).
 * Loaded once: tries filesystem path {@value #DEFAULT_FILESYSTEM_PATH} first; if not found, uses
 * classpath resource {@value #RESOURCE_PATH}. File encoding: UTF-8 (ASCII-compatible).
 */
public final class BCodes {

    private static final String RESOURCE_PATH = "dballe.txt";


    /** dballe format: optional space + 6-digit code, then description, then 2+ spaces, then unit and rest. */
    private static final Pattern SPLIT_REST = Pattern.compile("\\s{2,}");

    private static final class Holder {
        static final BCodes INSTANCE = load();
    }

    private final Map<String, CodeInfo> byCode = new HashMap<>();

    private BCodes() {}

    private static BCodes load() {
        BCodes loader = new BCodes();
         
            try {
                loader.loadFromClasspath();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load: " + e.getMessage(), e);
            }
        
        return loader;
    }

    /**
     * Loads B-codes from a file on the filesystem (same format as dballe.txt).
     * Uses UTF-8 encoding.
     *
     * @param path path to dballe.txt
     * @throws IOException if the file cannot be read
     */
    public void loadFromFileSystem(Path path) throws IOException {
        byCode.clear();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            parseLines(reader);
        }
    }

    private void loadFromClasspath() throws IOException {
        byCode.clear();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                parseLines(reader);
            }
        }
    }

    private void parseLines(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.length() < 6) continue;
            String sixDigit = line.substring(0, 6);
            if (!sixDigit.matches("\\d{6}")) continue;
            String bCode = "B" + sixDigit.substring(1);
            String rest = line.substring(6).trim();
            String[] parts = SPLIT_REST.split(rest, 2);
            String description = parts[0].trim();
            String unit = parts.length > 1 ? parseUnit(parts[1].trim()) : "";
            byCode.put(bCode, new CodeInfo(description, unit));
        }
    }

    /** Extract unit from the tail of a dballe line (tokens until we hit a numeric-only field). */
    private static String parseUnit(String tail) {
        if (tail.isEmpty()) return "";
        String[] tokens = tail.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (t.matches("\\d+")) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString().trim();
    }

    /** Returns the singleton instance that has loaded dballe.txt from the classpath. */
    public static BCodes getInstance() {
        return Holder.INSTANCE;
    }

    /** Description for the given B-code (e.g. B12101 → "TEMPERATURE/DRY-BULB TEMPERATURE"), or empty if unknown. */
    public Optional<String> getDescription(String code) {
        CodeInfo info = byCode.get(code);
        return info == null ? Optional.empty() : Optional.of(info.description);
    }

    /** Unit for the given B-code (e.g. K, %, M/S), or empty if unknown. */
    public Optional<String> getUnit(String code) {
        CodeInfo info = byCode.get(code);
        return info == null ? Optional.empty() : Optional.ofNullable(info.unit.isEmpty() ? null : info.unit);
    }

    /** Full code info (description + unit) for the given B-code, or empty if unknown. */
    public Optional<CodeInfo> getCodeInfo(String code) {
        return Optional.ofNullable(byCode.get(code));
    }

    /** Resolves observed property label: description from dballe.txt if present, otherwise the raw code. */
    public String getObservedPropertyLabel(String code) {
        return getDescription(code).orElse(code);
    }

    public static final class CodeInfo {
        private final String description;
        private final String unit;

        CodeInfo(String description, String unit) {
            this.description = description;
            this.unit = unit == null ? "" : unit;
        }

        public String getDescription() {
            return description;
        }

        public String getUnit() {
            return unit.isEmpty() ? null : unit;
        }
    }
}
