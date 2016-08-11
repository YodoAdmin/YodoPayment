package co.yodo.mobile.ui.option.factory;

import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.notification.YodoHandler;
import co.yodo.mobile.ui.option.AboutOption;
import co.yodo.mobile.ui.option.BalanceOption;
import co.yodo.mobile.ui.option.CloseAccountOption;
import co.yodo.mobile.ui.option.DeLinkAccountOption;
import co.yodo.mobile.ui.option.LinkAccountOption;
import co.yodo.mobile.ui.option.LinkCodeOption;
import co.yodo.mobile.ui.option.PaymentOption;
import co.yodo.mobile.ui.option.SaveCouponOption;
import co.yodo.mobile.ui.option.contract.IOption;

/**
 * Created by hei on 11/08/16.
 * Builds the different options
 */
public class OptionsFactory {
    /** Mandatory activity and messenger */
    private final MainActivity mActivity;
    private final YodoHandler mHandlerMessages;

    /** Options enumerate */
    public enum Option {
        SAVE_COUPON,
        ABOUT,
        PAYMENT,
        BALANCE,
        LINK_CODE,
        LINK_ACCOUNT,
        DE_LINK_ACCOUNT,
        CLOSE_ACCOUNT
    }

    /** Options */
    private SaveCouponOption mSaveCouponOption = null;
    private AboutOption mAboutOption = null;

    /** Options that executes a request */
    private PaymentOption mPaymentOption = null;
    private BalanceOption mBalanceOption = null;
    private LinkCodeOption mLinkCodeOption = null;
    private LinkAccountOption mLinkAccountOption = null;
    private DeLinkAccountOption mDeLinkAccountOption = null;
    private CloseAccountOption mCloseAccountOption = null;

    public OptionsFactory( MainActivity activity, YodoHandler handlerMessages ) {
        this.mActivity = activity;
        mHandlerMessages = handlerMessages;
    }

    public IOption getOption( Option option ) {
        switch( option ) {
            case SAVE_COUPON:
                if( mSaveCouponOption == null )
                    mSaveCouponOption = new SaveCouponOption( mActivity );
                return mSaveCouponOption;

            case ABOUT:
                if( mAboutOption == null )
                    mAboutOption = new AboutOption( mActivity );
                return mAboutOption;

            case PAYMENT:
                if( mPaymentOption == null )
                    mPaymentOption = new PaymentOption( mActivity, mHandlerMessages );
                return mPaymentOption;

            case BALANCE:
                if( mBalanceOption == null )
                    mBalanceOption = new BalanceOption( mActivity, mHandlerMessages );
                return mBalanceOption;

            case LINK_CODE:
                if( mLinkCodeOption == null )
                    mLinkCodeOption = new LinkCodeOption( mActivity, mHandlerMessages );
                return mLinkCodeOption;

            case LINK_ACCOUNT:
                if( mLinkAccountOption == null )
                    mLinkAccountOption = new LinkAccountOption( mActivity, mHandlerMessages );
                return mLinkAccountOption;

            case DE_LINK_ACCOUNT:
                if( mDeLinkAccountOption == null )
                    mDeLinkAccountOption = new DeLinkAccountOption( mActivity, mHandlerMessages );
                return mDeLinkAccountOption;

            case CLOSE_ACCOUNT:
                if( mCloseAccountOption == null )
                    mCloseAccountOption = new CloseAccountOption( mActivity, mHandlerMessages );
                return mCloseAccountOption;

            default:
                return null;
        }
    }
}
