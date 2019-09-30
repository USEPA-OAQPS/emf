package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.security.User;

public class ModuleTypeVersionParameter implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
    public static final int MAX_NAME_LEN = 53;

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String parameterName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private String sqlParameterType;

    private String description;

    private boolean isOptional;
    
    public boolean matchesImportedModuleTypeVersionParameter(String indent, final StringBuilder differences, ModuleTypeVersionParameter importedModuleTypeVersionParameter) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedModuleTypeVersionParameter)
            return result;

        String fullName = moduleTypeVersion.fullNameSDS("module type \"%s\" version %d \"%s\"");
        String importedFullName = importedModuleTypeVersionParameter.getModuleTypeVersion().fullNameSDS("module type \"%s\" version %d \"%s\"");

        // skipping id;

        // skipping moduleTypeVersion
        
        if (!parameterName.equals(importedModuleTypeVersionParameter.getParameterName())) { // should never happen
            differences.append(String.format("%sERROR: Local %s parameter name \"%s\" is different than imported %s parameter name \"%s\".\n",
                                             indent, fullName, parameterName, importedFullName, importedModuleTypeVersionParameter.getParameterName()));
            result = false;
        }
        
        if (!mode.equals(importedModuleTypeVersionParameter.getMode())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local %s parameter \"%s\" mode (%s) is different than imported %s parameter \"%s\" mode (%s).\n",
                                             indent, fullName, parameterName, mode, importedFullName,
                                             importedModuleTypeVersionParameter.getParameterName(), importedModuleTypeVersionParameter.getMode()));
            result = false;
        }
        
        if (!sqlParameterType.equals(importedModuleTypeVersionParameter.getSqlParameterType())) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local %s parameter \"%s\" SQL type (%s) is different than imported %s parameter \"%s\" SQL type (%s).\n",
                                             indent, fullName, parameterName, sqlParameterType, importedFullName,
                                             importedModuleTypeVersionParameter.getParameterName(), importedModuleTypeVersionParameter.getSqlParameterType()));
            result = false;
        }
        
        if ((description == null) != (importedModuleTypeVersionParameter.getDescription() == null) ||
           ((description != null) && !description.equals(importedModuleTypeVersionParameter.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local %s parameter \"%s\" description differs from the corresponding imported module type version parameter description.\n",
                                             indent, fullName, parameterName));
            // result = false;
        }

        if (isOptional != importedModuleTypeVersionParameter.getIsOptional()) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local %s parameter \"%s\" is %s while the imported %s parameter \"%s\" is %s.\n",
                                             indent, fullName, parameterName, isOptional ? "optional" : "not optional",
                                             importedFullName, importedModuleTypeVersionParameter.getParameterName(),
                                             importedModuleTypeVersionParameter.getIsOptional() ? "optional" : "not optional"));
            result = false;
        }

        return result;
    }
    
    public void prepareForExport() {
        id = 0;
    }
    
    public ModuleTypeVersionParameter deepCopy() {
        ModuleTypeVersionParameter newModuleTypeVersionParameter = new ModuleTypeVersionParameter();
        newModuleTypeVersionParameter.setModuleTypeVersion(moduleTypeVersion);
        newModuleTypeVersionParameter.setParameterName(parameterName);
        newModuleTypeVersionParameter.setMode(mode);
        newModuleTypeVersionParameter.setSqlParameterType(sqlParameterType);
        newModuleTypeVersionParameter.setDescription(description);
        newModuleTypeVersionParameter.setIsOptional(isOptional);
        return newModuleTypeVersionParameter;
    }

    public static boolean isValidParameterName(String name, final StringBuilder error) {
        error.setLength(0);
        name = name.trim();
        if (name.length() == 0) {
            error.append("Parameter name cannot be empty.");
            return false;
        }
        if (name.length() > MAX_NAME_LEN) {
            error.append(String.format("Parameter name '%s' is longer than %d characters.", name, MAX_NAME_LEN));
            return false;
        }
        Matcher matcher = Pattern.compile("[^a-zA-Z0-9_]", Pattern.CASE_INSENSITIVE).matcher(name);
        if (matcher.find()) {
            error.append(String.format("Parameter name '%s' contains illegal characters.", name));
            return false;
        }
        if (name.charAt(0) != '_' && !Character.isLetter(name.charAt(0))) {
            error.append(String.format("Parameter name '%s' must begin with a letter or _ (underscore).", name));
            return false;
        }
        return true;
    }

    public static boolean isValidSqlParameterType(String type, final StringBuilder error) {
        error.setLength(0);
        type = type.trim();
        if (type.length() == 0) {
            error.append("SQL paramater type cannot be empty.");
            return false;
        }
        // TODO add more SQL type checks here
        return true;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (!isValidParameterName(parameterName, error)) return false;
        if (!isValidSqlParameterType(sqlParameterType, error)) return false;
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName.trim();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isModeIN() {
        return this.mode.equals(IN);
    }

    public boolean isModeINOUT() {
        return this.mode.equals(INOUT);
    }

    public boolean isModeOUT() {
        return this.mode.equals(OUT);
    }

    public String getSqlParameterType() {
        return sqlParameterType;
    }

    public void setSqlParameterType(String sqlParameterType) {
        this.sqlParameterType = sqlParameterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public String toString() {
        return getParameterName();
    }

    public int hashCode() {
        return parameterName.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionParameter && ((ModuleTypeVersionParameter) other).getParameterName() == parameterName);
    }

    public int compareTo(ModuleTypeVersionParameter o) {
        return parameterName.compareTo(o.getParameterName());
    }
}
