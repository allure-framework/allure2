package io.qameta.allure.entity;

/**
 * @author Dmitry Baev baev@qameta.io
 *         Date: 26.02.16
 */
public interface WithDescription {

    String getDescription();

    void setDescription(String value);

    String getDescriptionHtml();

    void setDescriptionHtml(String value);

}
