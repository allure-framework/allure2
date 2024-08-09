// #IL_add Добавим дату

package io.qameta.allure.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class DateIl implements Serializable {

    protected String dateIl;

}
