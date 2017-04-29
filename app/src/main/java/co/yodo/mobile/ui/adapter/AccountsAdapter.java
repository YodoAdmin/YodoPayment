package co.yodo.mobile.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.yodo.mobile.R;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.PrefUtils;
import co.yodo.mobile.model.dtos.LinkedAccount;
import co.yodo.mobile.utils.GuiUtils;

/**
 * Created by hei on 11/11/16.
 * Adapter for the Linked accounts
 */
public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.LinkAccountViewHolder> {
    /** Data for the list */
    private List<LinkedAccount> accounts = new ArrayList<>();
    private List<LinkedAccount> accountsPendingRemoval;

    public AccountsAdapter( List<LinkedAccount> accounts ) {
        this.accounts = accounts;
        this.accountsPendingRemoval = new ArrayList<>();
    }

    @Override
    public LinkAccountViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View v = LayoutInflater.from( parent.getContext() ).inflate( R.layout.row_account, parent, false );
        return new AccountsAdapter.LinkAccountViewHolder( v );
    }

    @Override
    public void onBindViewHolder( final LinkAccountViewHolder holder, final int position ) {
        final LinkedAccount account = accounts.get( position );

        if (accountsPendingRemoval.contains(account)) {
            // Show the "undoLayout" state of the row
            holder.rlUndo.setVisibility( View.VISIBLE );
            holder.cvAccount.setVisibility( View.GONE );

            holder.tvUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // user wants to undoLayout the removal, let's cancel the pending task
                    accountsPendingRemoval.remove( account );

                    // this will rebind the row in "normal" state
                    notifyItemChanged( accounts.indexOf( account ) );
                }
            });
        } else {
            // Show the "entryLogLayout" state of the row
            holder.rlUndo.setVisibility( View.GONE );
            holder.cvAccount.setVisibility( View.VISIBLE );

            // Set the data
            holder.tvAccountTitle.setText( account.getNickname() );

            // Set on click listener
            holder.cvAccount.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    final Activity ac = GuiUtils.getActivity( v );
                    if( ac == null ) {
                        return;
                    }

                    // Dialog layout
                    LayoutInflater inflater = (LayoutInflater) ac.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                    final View layout = inflater.inflate( R.layout.dialog_with_nickname, new LinearLayout( ac ), false );
                    final TextInputEditText etInput = (TextInputEditText) layout.findViewById( R.id.etNickname );

                    // Set nickname
                    etInput.setText( PrefUtils.getNickname( account.getHardwareToken() ) );
                    etInput.setSelection( etInput.getText().length() );

                    // Prepare on click listener
                    final DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i ) {
                            // Get the nickname from the EditText
                            String nickname = etInput.getText().toString();
                            if( nickname.isEmpty() ) {
                                nickname = null;
                            }

                            // Save the new nickname
                            PrefUtils.saveNickname( account.getHardwareToken(), nickname );
                            account.setNickname( PrefUtils.getNickname( account.getHardwareToken() ) );
                            notifyItemChanged( holder.getAdapterPosition() );
                        }
                    };

                    AlertDialogHelper.show(
                            ac,
                            layout,
                            okClick
                    );
                }
            } );
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public void onAttachedToRecyclerView( RecyclerView recyclerView ) {
        super.onAttachedToRecyclerView( recyclerView );
    }

    /**
     * Add the item to a pending removal
     * @param position The position to remove it
     */
    public void pendingRemoval( int position ) {
        // this will redraw row in "undoLayout" state
        final LinkedAccount item = accounts.get( position );
        if( !accountsPendingRemoval.contains( item ) ) {
            accountsPendingRemoval.add( item );
            notifyItemChanged( position );
        }
    }

    /**
     * Removes an element from the view
     * @param position The position
     */
    public void remove( int position ) {
        final LinkedAccount item = accounts.get( position );
        if( accountsPendingRemoval.contains( item ) ) {
            accountsPendingRemoval.remove( item );
        }

        if( accounts.contains( item ) ) {
            accounts.remove( position );
            notifyItemRemoved( position );
        }
    }

    /**
     * Verify if it is pending for removal
     * @param position The position in the original list
     * @return if it is in the removal list
     */
    public boolean isPendingRemoval( int position ) {
        LinkedAccount item = accounts.get( position );
        return accountsPendingRemoval.contains( item );
    }

    /**
     * Get the accounts waiting for removal
     * @return The list with the accounts that will be removed
     */
    public List<LinkedAccount> getAccountsPendingRemoval() {
        return this.accountsPendingRemoval;
    }

    static class LinkAccountViewHolder extends RecyclerView.ViewHolder {
        /** GUI Controllers */
        @BindView(R.id.cvAccount )
        CardView cvAccount;

        @BindView( R.id.tvLinkedAccount )
        TextView tvAccountTitle;

        /** Entry delete row controllers */
        @BindView(R.id.rlUndo )
        RelativeLayout rlUndo;

        @BindView(R.id.tvUndo )
        TextView tvUndo;

        LinkAccountViewHolder( View itemView ) {
            super( itemView );

            // Injection
            ButterKnife.bind(this, itemView);
        }
    }
}
