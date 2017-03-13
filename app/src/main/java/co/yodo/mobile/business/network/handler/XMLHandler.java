package co.yodo.mobile.business.network.handler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import co.yodo.mobile.business.network.model.ServerResponse;

/**
 * Created by luis on 15/12/14.
 * Handler for the XML responses
 */
public class XMLHandler extends DefaultHandler {
    /** XML root element */
    private static final String ROOT_ELEMENT = "Yodoresponse";

    /** XML sub root element */
    private static final String CODE_ELEM     = "code";
    private static final String AUTH_NUM_ELEM = "authNumber";
    private static final String MESSAGE_ELEM  = "message";
    private static final String TIME_ELEM     = "rtime";

    /** Param elements */
    private static final String LOGO_ELEM         = "logo_url";
    private static final String BALANCE_ELEM      = "balance";
    private static final String CURRENCY_ELEM     = "currency"; // It also belongs to the receipt
    private static final String BIOMETRIC_ELEM    = "BiometricToken";
    private static final String ADVERTISING_ELEM  = "url";
    private static final String LINKING_CODE_ELEM = "linking_code";
    private static final String TRANSACTION_ELEM  = "LastSuccessfulTransaction";
    private static final String LINKED_ACC_ELEM   = "linked_accounts";

    /** Transaction Elements */
    public static final String YI = "yi";
    public static final String YT = "yt";

    /** Merchant elements */
    private static final String DESCRIPTION_ELEM = "description";
    private static final String DCURRENCY_ELEM   = "dcurrency";

    /** Receipt elements */
    private static final String CREATED_ELEM     = "created";
    private static final String AMOUNT_ELEM      = "amount";
    private static final String TAMOUNT_ELEM     = "tamount";
    private static final String CASHBACK_ELEM    = "cashback";
    private static final String AUTHNUMBER_ELEM  = "transauthnumber";
    private static final String EXCHA_RATE_ELEM  = "xch_rate";

    /** Linked Account elements */
    private static final String TO_ELEM   = "to";
    private static final String FROM_ELEM = "from";

    /** Parser Elements */
    private Boolean currentElement = false;
    private String currentValue = null;

    /** Is Receipt */
    private Boolean transactionElement = false;
    private Boolean merchantElement    = false;
    private Boolean receiptElement     = false;

    /** Is Linked Accounts */
    private Boolean linkedAccountElement = false;

    /** Server Response POJO */
    public static ServerResponse response = null;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = true;

        /* Start */
        if( localName.equalsIgnoreCase( ROOT_ELEMENT ) ) {
            response = new ServerResponse();
        } else if( localName.equals( TRANSACTION_ELEM ) ) {
            transactionElement = true;
        }
        /* Receipt */
        else if( transactionElement ) {
            if( localName.equalsIgnoreCase( YI ) ) {
                merchantElement = true;
            }
            else if( localName.equalsIgnoreCase( YT ) ) {
                receiptElement = true;
            }
        }
        else if( localName.equals( LINKED_ACC_ELEM ) ) {
            linkedAccountElement = true;
        }

    }

    /** Called when tag closing ( ex:- <name>AndroidPeople</name>
     * -- </name> )*/
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = false;

        /* Set value */
        /*if( localName.equalsIgnoreCase( CODE_ELEM ) ) {
            response.setCode( currentValue );
        }
        else if( localName.equalsIgnoreCase( AUTH_NUM_ELEM ) ) {
            response.setAuthNumber( currentValue );
        }
        else if( localName.equalsIgnoreCase( MESSAGE_ELEM ) ) {
            response.setMessage( currentValue );
        }
        else if( localName.equalsIgnoreCase( TIME_ELEM ) ) {
            response.setRTime( Long.valueOf( currentValue ) );
        }*/

        /* Close TAGs */
        /*else if( localName.equals( TRANSACTION_ELEM ) ) {
            transactionElement = false;
        }
        else if( localName.equals( LINKED_ACC_ELEM ) ) {
            linkedAccountElement = false;
        }

        // Receipt
        else if( transactionElement ) {
            if( localName.equalsIgnoreCase( YI ) ) {
                merchantElement = false;
            }
            else if( localName.equalsIgnoreCase( YT ) ) {
                receiptElement = false;
            }
            else if( merchantElement ) {
                if( localName.equalsIgnoreCase( DESCRIPTION_ELEM ) ) {
                    response.addParam( ServerResponse.DESCRIPTION, currentValue );
                }
                else if( localName.equalsIgnoreCase( DCURRENCY_ELEM ) ) {
                    response.addParam( ServerResponse.DCURRENCY, currentValue );
                }
            }
            else if( receiptElement ) {
                if( localName.equalsIgnoreCase( CREATED_ELEM ) ) {
                    response.addParam( ServerResponse.CREATED, currentValue );
                }
                else if( localName.equalsIgnoreCase( AMOUNT_ELEM ) ) {
                    response.addParam( ServerResponse.AMOUNT, currentValue );
                }
                else if( localName.equalsIgnoreCase( TAMOUNT_ELEM ) ) {
                    response.addParam( ServerResponse.TAMOUNT, currentValue );
                }
                else if( localName.equalsIgnoreCase( CASHBACK_ELEM ) ) {
                    response.addParam( ServerResponse.CASHBACK, currentValue );
                }
                else if( localName.equalsIgnoreCase( AUTHNUMBER_ELEM ) ) {
                    response.addParam( ServerResponse.AUTHNUMBER, currentValue );
                }
                else if( localName.equalsIgnoreCase( CURRENCY_ELEM ) ) {
                    response.addParam( ServerResponse.TCURRENCY, currentValue );
                }
                else if( localName.equalsIgnoreCase( EXCHA_RATE_ELEM ) ) {
                    response.addParam( ServerResponse.EXCH_RATE, currentValue );
                }
            }

            else if( localName.equalsIgnoreCase( BALANCE_ELEM ) ) {
                response.addParam( ServerResponse.BALANCE, currentValue );
            }
            else if( localName.equalsIgnoreCase( CURRENCY_ELEM ) ) {
                response.addParam( ServerResponse.CURRENCY, currentValue );
            }
        }

        // Linked Accounts
        else if( linkedAccountElement ) {
            if( localName.equalsIgnoreCase( TO_ELEM ) ) {
                response.addParam( ServerResponse.TO, currentValue );
            }
            else if( localName.equalsIgnoreCase( FROM_ELEM ) ) {
                response.addParam( ServerResponse.FROM, currentValue );
            }
        }

        // Params
        else if( localName.equalsIgnoreCase( LOGO_ELEM ) ) {
            response.addParam( ServerResponse.LOGO, currentValue );
        }
        else if( localName.equalsIgnoreCase( BALANCE_ELEM ) ) {
            response.addParam( ServerResponse.BALANCE, currentValue );
        }
        else if( localName.equalsIgnoreCase( CURRENCY_ELEM ) ) {
            response.addParam( ServerResponse.CURRENCY, currentValue );
        }
        else if( localName.equalsIgnoreCase( BIOMETRIC_ELEM ) ) {
            response.addParam( ServerResponse.BIOMETRIC, currentValue );
        }
        else if( localName.equalsIgnoreCase( ADVERTISING_ELEM ) ) {
            response.addParam( ServerResponse.ADVERTISING, currentValue );
        }
        else if( localName.equalsIgnoreCase( LINKING_CODE_ELEM ) ) {
            response.addParam( ServerResponse.LINKING_CODE, currentValue );
        }

        currentValue = "";*/
    }

    /** Called to get tag characters ( ex:- <name>AndroidPeople</name>
     * -- to get AndroidPeople Character ) */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if( currentElement ) {
            currentValue = new String( ch, start, length );
            currentElement = false;
        }
    }
}
