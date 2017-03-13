package co.yodo.mobile.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.FormatUtils;
import co.yodo.mobile.ui.dialog.contract.IDialog;

/**
 * Created by hei on 16/06/16.
 * builds a receipt dialog from data
 */
public class ReceiptDialog extends IDialog {
    /**
     * Builds a Dialog for the receipt data
     * @param builder The ReceiptBuilder
     */
    private ReceiptDialog( Builder builder ) {
        super( builder );
    }

    /**
     * Builder for the receipts
     */
    public static class Builder extends DialogBuilder {
        /** GUI Controllers */
        private TextView tvDescription;
        private TextView tvCreated;
        private TextView tvTotalAmount;
        private TextView tvAuthnumber;
        private TextView tvTenderAmount;
        private TextView tvCashbackAmount;
        private TextView tvDonorAccount;
        private TextView tvRecipientAccount;
        private LinearLayout llDonor;
        private ImageView ivDelete;
        private ImageView ivSave;

        /** Builds the receipt main components */
        public Builder( Context context ) {
            super( context, R.layout.dialog_receipt );

            // Data
            this.tvDescription      = (TextView) this.dialog.findViewById( R.id.descriptionText );
            this.tvCreated          = (TextView) this.dialog.findViewById( R.id.createdText );
            this.tvTotalAmount      = (TextView) this.dialog.findViewById( R.id.paidText );
            this.tvAuthnumber       = (TextView) this.dialog.findViewById( R.id.authNumberText );
            this.tvTenderAmount     = (TextView) this.dialog.findViewById( R.id.cashTenderText );
            this.tvCashbackAmount   = (TextView) this.dialog.findViewById( R.id.cashBackText );
            this.tvDonorAccount     = (TextView) this.dialog.findViewById( R.id.tvDonorText );
            this.tvRecipientAccount = (TextView) this.dialog.findViewById( R.id.tvReceiverText );

            // Layout
            this.llDonor = (LinearLayout) this.dialog.findViewById( R.id.donorAccountLayout );

            // Buttons
            this.ivSave   = (ImageView) this.dialog.findViewById( R.id.saveButton );
            this.ivDelete = (ImageView) this.dialog.findViewById( R.id.deleteButton );
        }

        @Override
        public Builder cancelable( boolean cancelable ) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder description( String description ) {
            tvDescription.setText( description );
            return this;
        }

        public Builder created( String created ) {
            tvCreated.setText(
                    FormatUtils.UTCtoCurrent( this.context, created )
            );
            return this;
        }

        public Builder total( String total, String currency ) {
            tvTotalAmount.setText(
                    String.format( "%s %s",
                            FormatUtils.truncateDecimal( total ),
                            currency
                    )
            );
            return this;
        }

        public Builder authnumber( String authnumber ) {
            tvAuthnumber.setText( authnumber );
            return this;
        }

        public Builder donor( String donor ) {
            if( donor != null ) {
                this.llDonor.setVisibility( View.VISIBLE );
                this.tvDonorAccount.setText( donor );
            }
            return this;
        }

        public Builder recipient( String recipient ) {
            this.tvRecipientAccount.setText( recipient );
            return this;
        }

        public Builder tender( String tender, String currency ) {
            tvTenderAmount.setText(
                    String.format( "%s %s",
                            FormatUtils.truncateDecimal( tender ),
                            currency
                    )
            );
            return this;
        }

        public Builder cashback( String cashback, String currency ) {
            tvCashbackAmount.setText(
                    String.format( "%s %s",
                        FormatUtils.truncateDecimal( cashback ),
                        currency
                    )
            );
            return this;
        }

        public Builder save( final View.OnClickListener onClick ) {
            this.ivSave.setVisibility( View.VISIBLE );
            this.ivSave.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    onClick.onClick( v );
                    dialog.dismiss();
                }
            } );
            return this;
        }

        public Builder delete( final View.OnClickListener onClick ) {
            this.ivDelete.setVisibility( View.VISIBLE );
            this.ivDelete.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    onClick.onClick( v );
                    dialog.dismiss();
                }
            } );
            return this;
        }

        @Override
        public ReceiptDialog build() {
            return new ReceiptDialog( this );
        }
    }
}
