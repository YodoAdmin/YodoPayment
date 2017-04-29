package co.yodo.mobile.ui.option.factory;

import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.ui.BaseActivity;
import co.yodo.mobile.ui.option.AboutOption;
import co.yodo.mobile.ui.option.BalanceOption;
import co.yodo.mobile.ui.option.CloseAccountOption;
import co.yodo.mobile.ui.option.LinkedAccountsOption;
import co.yodo.mobile.ui.option.LinkAccountOption;
import co.yodo.mobile.ui.option.LinkingCodeOption;
import co.yodo.mobile.ui.option.PaymentOption;
import co.yodo.mobile.ui.option.ResetPipOption;
import co.yodo.mobile.ui.option.SaveCouponOption;
import co.yodo.mobile.ui.option.contract.IOption;

/**
 * Created by hei on 11/08/16.
 * Builds the different options
 */
public class OptionsFactory {
    /** Mandatory activity and messenger */
    private final BaseActivity activity;

    /** Options enumerate */
    public enum Option {
        PAYMENT,
        COUPONS,
        ABOUT,
        BALANCE,
        RESET_PIP,
        LINKING_CODE,
        LINK_ACCOUNT,
        LINKED_ACCOUNTS,
        CLOSE_ACCOUNT
    }

    /** Options */
    private SaveCouponOption saveCouponOption = null;
    private AboutOption aboutOption = null;

    /** Options that executes a request */
    private PaymentOption paymentOption = null;
    private BalanceOption balanceOption = null;
    private ResetPipOption resetPipOption = null;
    private LinkingCodeOption linkingCodeOption = null;
    private LinkAccountOption linkAccountOption = null;
    private LinkedAccountsOption listLinkAccountsOption = null;
    private CloseAccountOption closeAccountOption = null;

    public OptionsFactory( BaseActivity activity ) {
        this.activity = activity;
    }

    public IOption getOption( Option option ) {
        // Stop the subscribing for any option except coupons
        if( option != Option.COUPONS ) {
            PrefUtils.setSubscribing( activity, false );
        }

        switch( option ) {
            case PAYMENT:
                if( paymentOption == null ) {
                    paymentOption = new PaymentOption( activity );
                }
                return paymentOption;

            case COUPONS:
                if( saveCouponOption == null ) {
                    saveCouponOption = new SaveCouponOption( activity );
                }
                return saveCouponOption;

            case ABOUT:
                if( aboutOption == null ) {
                    aboutOption = new AboutOption( activity );
                }
                return aboutOption;

            case BALANCE:
                if( balanceOption == null ) {
                    balanceOption = new BalanceOption( activity );
                }
                return balanceOption;

            case RESET_PIP:
                if( resetPipOption == null ) {
                    resetPipOption = new ResetPipOption( activity );
                }
                return resetPipOption;

            case LINKING_CODE:
                if( linkingCodeOption == null ) {
                    linkingCodeOption = new LinkingCodeOption( activity );
                }
                return linkingCodeOption;

            case LINK_ACCOUNT:
                if( linkAccountOption == null )
                    linkAccountOption = new LinkAccountOption( activity );
                return linkAccountOption;

            case LINKED_ACCOUNTS:
                if( listLinkAccountsOption == null ) {
                    listLinkAccountsOption = new LinkedAccountsOption( activity );
                }
                return listLinkAccountsOption;

            case CLOSE_ACCOUNT:
                if( closeAccountOption == null ) {
                    closeAccountOption = new CloseAccountOption( activity );
                }
                return closeAccountOption;

            default:
                return null;
        }
    }
}
