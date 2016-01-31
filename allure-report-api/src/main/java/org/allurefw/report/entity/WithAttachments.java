package org.allurefw.report.entity;

import org.allurefw.report.Attachment;

import java.util.List;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.01.16
 */
public interface WithAttachments {

    List<Attachment> getAttachments();

}
