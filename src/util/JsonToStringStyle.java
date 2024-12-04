package util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class JsonToStringStyle extends ToStringStyle {

    private static final String FULL_DETAIL_MUST_BE_TRUE = "FullDetail must be true when using JsonToStringStyle";

	private static final String FIELD_NAMES_ARE_MANDATORY = "Field names are mandatory when using JsonToStringStyle";

	private static final long serialVersionUID = 1L;

    private static final String FIELD_NAME_QUOTE = "\"";

    /**
     * Constructs a new instance.
     *
     * <p>
     * Use the static constant rather than instantiating.
     * </p>
     */
    public JsonToStringStyle() {
        setUseIdentityHashCode(false);

        setContentStart("{");
        setContentEnd("}}");

        setArrayStart("[");
        setArrayEnd("]");

        setFieldSeparator(",");
        setFieldNameValueSeparator(":");

        setNullText("null");

        setSummaryObjectStartText("\"<");
        setSummaryObjectEndText(">\"");

        setSizeStartText("\"<size=");
        setSizeEndText(">\"");
    }
    
    @Override
    protected void appendClassName(final StringBuffer buffer, final Object object) {
        if (object != null) {
            buffer.append("{\"").append(getShortClassName(object.getClass())).append("\":");
        }
    }
    @Override
    public void append(final StringBuffer buffer, final String fieldName,
                       final boolean[] array, final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName, final byte[] array,
                       final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName, final char[] array,
                       final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName,
                       final double[] array, final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName,
                       final float[] array, final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName, final int[] array,
                       final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName, final long[] array,
                       final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName, final Object value,
                       final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, value, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName,
                       final Object[] array, final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    public void append(final StringBuffer buffer, final String fieldName,
                       final short[] array, final Boolean fullDetail) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }
        if (!isFullDetail(fullDetail)) {
            throw new UnsupportedOperationException(
                    FULL_DETAIL_MUST_BE_TRUE);
        }

        super.append(buffer, fieldName, array, fullDetail);
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final char value) {
        appendValueAsString(buffer, String.valueOf(value));
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Collection<?> coll) {
        if (coll != null && !coll.isEmpty()) {
            buffer.append(getArrayStart());
            int i = 0;
            for (final Object item : coll) {
                appendDetail(buffer, fieldName, i++, item);
            }
            buffer.append(getArrayEnd());
            return;
        }

        buffer.append(coll);
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Map<?, ?> map) {
        if (map != null && !map.isEmpty()) {
            buffer.append(getContentStart());

            boolean firstItem = true;
            for (final Entry<?, ?> entry : map.entrySet()) {
                final String keyStr = Objects.toString(entry.getKey(), null);
                if (keyStr != null) {
                    if (firstItem) {
                        firstItem = false;
                    } else {
                        appendFieldEnd(buffer, keyStr);
                    }
                    appendFieldStart(buffer, keyStr);
                    final Object value = entry.getValue();
                    if (value == null) {
                        appendNullText(buffer, keyStr);
                    } else {
                        appendInternal(buffer, keyStr, value, true);
                    }
                }
            }

            buffer.append(getContentEnd());
            return;
        }

        buffer.append(map);
    }

    @Override
    protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {

        if (value == null) {
            appendNullText(buffer, fieldName);
            return;
        }

        if (value instanceof String || value instanceof Character) {
            appendValueAsString(buffer, value.toString());
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            buffer.append(value);
            return;
        }

        final String valueAsString = value.toString();
        if (isJsonObject(valueAsString) || isJsonArray(valueAsString)) {
            buffer.append(value);
            return;
        }

        appendDetail(buffer, fieldName, valueAsString);
    }

    @Override
    protected void appendFieldStart(final StringBuffer buffer, final String fieldName) {

        if (fieldName == null) {
            throw new UnsupportedOperationException(
                    FIELD_NAMES_ARE_MANDATORY);
        }

        super.appendFieldStart(buffer, FIELD_NAME_QUOTE + StringEscapeUtils.escapeJson(fieldName)
                + FIELD_NAME_QUOTE);
    }

    /**
     * Appends the given String enclosed in double-quotes to the given StringBuffer.
     *
     * @param buffer the StringBuffer to append the value to.
     * @param value the value to append.
     */
    private void appendValueAsString(final StringBuffer buffer, final String value) {
        buffer.append('"').append(StringEscapeUtils.escapeJson(value)).append('"');
    }

    private boolean isJsonArray(final String valueAsString) {
        return valueAsString.startsWith(getArrayStart())
                && valueAsString.endsWith(getArrayEnd());
    }

    private boolean isJsonObject(final String valueAsString) {
        return valueAsString.startsWith(getContentStart())
                && valueAsString.endsWith(getContentEnd());
    }

    /**
     * Ensure Singleton after serialization.
     *
     * @return the singleton
     */
    private Object readResolve() {
        return JSON_STYLE;
    }

}
