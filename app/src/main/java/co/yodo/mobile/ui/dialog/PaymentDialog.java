package co.yodo.mobile.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import co.yodo.mobile.R;
import co.yodo.mobile.ui.dialog.contract.IDialog;

/**
 * Created by hei on 17/06/16.
 * builds a payment dialog to choose the method of payment
 */
public class PaymentDialog extends IDialog {
    /** Payment types */
    public enum Payment {
        YODO        ( "0" ),
        CREDIT_VISA ( "1" ),
        STATIC      ( "2" ),
        HEART       ( "3" ),
        DEBIT_VISA  ( "4" ),
        PAYPAL      ( "5" );

        private final String value;

        Payment( String value ) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /**
     * Constructor that shows the dialog
     * based in the DialogBuilder
     * @param builder The DialogBuilder
     */
    private PaymentDialog( Builder builder ) {
        super( builder );
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private final ImageView ivYodoPayment;
        private final ImageView ivHeartPayment;

        /**
         * Builder constructor with the mandatory elements
         * @param context The application context
         */
        public Builder( Context context ) {
            super( context, R.layout.dialog_payment, R.style.AppCompatAlertDialogStyle );

            // Data
            this.ivYodoPayment = ( ImageView ) this.dialog.findViewById( R.id.ivYodo );
            this.ivHeartPayment = ( ImageView ) this.dialog.findViewById( R.id.ivHeart );

            // Set types
            this.ivYodoPayment.setTag( Payment.YODO );
            this.ivHeartPayment.setTag( Payment.HEART );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder action( final OnClickListener onClick ) {
            View.OnClickListener action = new View.OnClickListener() {
                @Override
                public void onClick( View paymentImage ) {
                    onClick.onClick( (Payment) paymentImage.getTag() );
                    dialog.dismiss();
                }
            };

            this.ivYodoPayment.setOnClickListener( action );
            this.ivHeartPayment.setOnClickListener( action );
            return this;
        }

        @Override
        public PaymentDialog build() {
            return new PaymentDialog( this );
        }
    }

    public interface OnClickListener {
        /**
         * Let the listener know the payment method selected
         * @param type The selected payment method
         */
        void onClick( Payment type );
    }
}
