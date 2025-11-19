package com.example.bemax.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bemax.R;
import com.example.bemax.model.domain.EmergencyContact;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder> {
    
    private List<EmergencyContact> contacts;
    private final Context context;
    private OnContactInteractionListener listener;
    private final Map<String, Integer> relationshipStringMap;

    public interface OnContactInteractionListener {
        void onContactClick(EmergencyContact contact);
        void onEditClick(EmergencyContact contact);
        void onDeleteClick(EmergencyContact contact);
    }

    public EmergencyContactAdapter(Context context) {
        this.context = context;
        this.contacts = new ArrayList<>();
        
        // Initialize relationship mapping (backend value -> string resource ID)
        relationshipStringMap = new HashMap<>();
        relationshipStringMap.put("spouse", R.string.relationship_spouse);
        relationshipStringMap.put("parent", R.string.relationship_parent);
        relationshipStringMap.put("child", R.string.relationship_child);
        relationshipStringMap.put("sibling", R.string.relationship_sibling);
        relationshipStringMap.put("friend", R.string.relationship_friend);
        relationshipStringMap.put("doctor", R.string.relationship_doctor);
        relationshipStringMap.put("caregiver", R.string.relationship_caregiver);
        relationshipStringMap.put("other", R.string.relationship_other);
    }

    public void setContacts(List<EmergencyContact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnContactInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView contactCard;
        private final TextView txtAvatarInitials;
        private final Chip chipPrimary;
        private final TextView txtContactName;
        private final TextView txtContactRelationship;
        private final TextView txtContactPhone;
        private final ImageView btnMore;
        private final LinearLayout actionButtons;
        private final MaterialButton btnCall;
        private final MaterialButton btnMessage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactCard = itemView.findViewById(R.id.contactCard);
            txtAvatarInitials = itemView.findViewById(R.id.txtAvatarInitials);
            chipPrimary = itemView.findViewById(R.id.chipPrimary);
            txtContactName = itemView.findViewById(R.id.txtContactName);
            txtContactRelationship = itemView.findViewById(R.id.txtContactRelationship);
            txtContactPhone = itemView.findViewById(R.id.txtContactPhone);
            btnMore = itemView.findViewById(R.id.btnMore);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMessage = itemView.findViewById(R.id.btnMessage);
        }

        public void bind(EmergencyContact contact) {
            // Set avatar initials
            String initials = getInitials(contact.getName());
            txtAvatarInitials.setText(initials);

            // Show/hide primary badge
            chipPrimary.setVisibility(contact.isPrimary() ? View.VISIBLE : View.GONE);

            // Set contact info
            txtContactName.setText(contact.getName());
            txtContactRelationship.setText(getLocalizedRelationship(contact.getRelationship()));
            txtContactPhone.setText(formatPhone(contact.getPhone()));

            // Card click
            contactCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(contact);
                }
            });

            // Phone number click - make call
            txtContactPhone.setOnClickListener(v -> makeCall(contact.getPhone()));

            // More options button
            btnMore.setOnClickListener(v -> showOptionsMenu(contact));

            // Action buttons
            btnCall.setOnClickListener(v -> makeCall(contact.getPhone()));
            btnMessage.setOnClickListener(v -> sendMessage(contact.getPhone()));
        }

        private String getInitials(String name) {
            if (name == null || name.isEmpty()) return "?";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            } else {
                return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            }
        }
        
        private String getLocalizedRelationship(String backendValue) {
            if (backendValue == null || backendValue.isEmpty()) {
                return context.getString(R.string.relationship_other);
            }
            
            // Get string resource ID from map
            Integer stringResId = relationshipStringMap.get(backendValue.toLowerCase());
            
            // Return localized string or the original value if not found
            if (stringResId != null) {
                return context.getString(stringResId);
            } else {
                // Fallback: capitalize first letter
                return backendValue.substring(0, 1).toUpperCase() + backendValue.substring(1).toLowerCase();
            }
        }

        private String formatPhone(String phone) {
            if (phone == null || phone.isEmpty()) return "";
            // Remove all non-digit characters
            String digits = phone.replaceAll("[^\\d]", "");
            
            // Format based on length
            if (digits.length() == 11) {
                return String.format("(%s) %s-%s", 
                    digits.substring(0, 2), 
                    digits.substring(2, 7), 
                    digits.substring(7));
            } else if (digits.length() == 10) {
                return String.format("(%s) %s-%s", 
                    digits.substring(0, 2), 
                    digits.substring(2, 6), 
                    digits.substring(6));
            }
            return phone;
        }

        private void showOptionsMenu(EmergencyContact contact) {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, btnMore);
            popup.getMenuInflater().inflate(R.menu.menu_contact_options, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditClick(contact);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteClick(contact);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void makeCall(String phone) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            context.startActivity(intent);
        }

        private void sendMessage(String phone) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phone));
            context.startActivity(intent);
        }
    }
}

