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
            this.ivYodoPayment = ( ImageView ) this.mDialog.findViewById( R.id.ivYodoPayment );
            this.ivHeartPayment = ( ImageView ) this.mDialog.findViewById( R.id.ivHeartPayment );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.mCancelable = cancelable;
            return this;
        }

        public Builder action( final View.OnClickListener onClick ) {
            View.OnClickListener action = new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    onClick.onClick( v );
                    mDialog.dismiss();
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
}
