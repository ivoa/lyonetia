package cds.adql.validation.jcommander;

import com.beust.jcommander.*;

import java.util.EnumSet;
import java.util.List;

public class CustomUsageFormatter extends UnixStyleUsageFormatter {

    public CustomUsageFormatter(JCommander commander) {
        super(commander);
    }

    @Override
    public void appendMainLine(StringBuilder out, boolean hasOptions, boolean hasCommands, int indentCount, String indent) {
        String mainLine = indent + "Usage: java -jar adql_validator.jar" +
                          " [OPTION]..." +
                          " (FILE|DIRECTORY)...";
        wrapDescription(out, 4, mainLine);
        out.append(System.lineSeparator()).append(System.lineSeparator());
        wrapDescription(out, 4, "Description:" + System.lineSeparator() + "    Validate the validation sets described in the given files. A file item can be a regular file or a directory. If a directory, all of its direct regular child files are tested ; use the recursive option to also explore its child repositories.");
        out.append(System.lineSeparator()).append(System.lineSeparator());
    }

    public void appendAllParametersDetails(StringBuilder out, int indentCount, String indent,
                                           List<ParameterDescription> sortedParameters) {
        if (sortedParameters.size() > 0) {
            out.append(indent).append("Options:").append(System.lineSeparator());
        }

        // Calculate prefix indent
        int prefixIndent = 0;

        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();
            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();

            if (prefix.length() > prefixIndent) {
                prefixIndent = prefix.length();
            }
        }

        // Append parameters
        for (ParameterDescription pd : sortedParameters) {
            WrappedParameter parameter = pd.getParameter();

            String prefix = (parameter.required() ? "* " : "  ") + pd.getNames();
            out.append(indent)
                    .append("  ")
                    .append(prefix)
                    .append(s(prefixIndent - prefix.length()))
                    .append(" ");
            final int initialLinePrefixLength = indent.length() + prefixIndent + 3;

            // Generate description
            String description = pd.getDescription();
            Object def = pd.getDefault();

            if (pd.isDynamicParameter()) {
                String syntax = "(syntax: " + parameter.names()[0] + "key" + parameter.getAssignment() + "value)";
                description += (description.length() == 0 ? "" : " ") + syntax;
            }

            if (def != null && !pd.isHelp()) {
                String displayedDef = Strings.isStringEmpty(def.toString()) ? "<empty string>" : def.toString();
                String defaultText = "(default: " + (parameter.password() ? "********" : displayedDef) + ")";
                description += (description.length() == 0 ? "" : " ") + defaultText;
            }
            Class<?> type = pd.getParameterized().getType();

            if (type.isEnum()) {
                String valueList = EnumSet.allOf((Class<? extends Enum>) type).toString();

                // Prevent duplicate values list, since it is set as 'Options: [values]' if the description
                // of an enum field is empty in ParameterDescription#init(..)
                if (!description.contains("Options: " + valueList)) {
                    String possibleValues = "(values: " + valueList + ")";
                    description += (description.length() == 0 ? "" : " ") + possibleValues;
                }
            }

            // Append description
            // The magic value 3 is the number of spaces between the name of the option and its description
            // in DefaultUsageFormatter#appendCommands(..)
            wrapDescription(out, indentCount + prefixIndent - 3, initialLinePrefixLength, description);
            out.append(System.lineSeparator());
        }
    }

}
