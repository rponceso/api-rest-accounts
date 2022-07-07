/**
 * Bean Stores Customer Information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private String id;
    private String customerType;
}
