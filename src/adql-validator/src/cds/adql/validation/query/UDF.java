package cds.adql.validation.query;

import adql.db.FunctionDef;
import adql.parser.feature.LanguageFeature;
import adql.parser.grammar.ParseException;

import java.util.Objects;

/**
 * Definition of a User Defined Function.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (01/2023)
 */
public class UDF {

    protected String form = null;

    protected LanguageFeature feature = null;

    public String description = null;

    public String getForm() {
        return form;
    }

    public void setForm(String newForm) throws ParseException {
        form    = null;
        feature = null;
        if (newForm != null && newForm.trim().length() > 0) {
            newForm = newForm.trim().replaceAll("[\n\t\r]", " ").replaceAll(" +", " ");
            feature = FunctionDef.parse(newForm).toLanguageFeature();
            form    = newForm;
        }
    }

    public LanguageFeature getFeature() {
        return feature;
    }

    @Override
    public String toString() {
        return "Function{form='" + form + '\'' + ", description=" + description.replaceAll("'", "\\'") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UDF udf = (UDF) o;
        return Objects.equals(form, udf.form);
    }

    @Override
    public int hashCode() {
        return Objects.hash(form);
    }
}
