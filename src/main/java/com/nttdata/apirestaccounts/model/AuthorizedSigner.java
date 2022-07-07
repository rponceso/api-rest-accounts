/**
 * Bean Stores AuthorizedSigner Information
 *
 * @author Renato Ponce
 * @version 1.0
 * @since 2022-06-24
 */

package com.nttdata.apirestaccounts.model;

import lombok.Data;

@Data
public class AuthorizedSigner {
    private String name;
    private String lastname;
    private String numberDocument;
    private String email;
}
