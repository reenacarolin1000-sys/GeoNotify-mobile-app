package com.example.geonotify.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geonotify.R
import com.example.geonotify.data.GeofenceEntity
import com.example.geonotify.data.GeofenceType

class GeofenceAdapter(private val onDeleteClick: (GeofenceEntity) -> Unit) :
    ListAdapter<GeofenceEntity, GeofenceAdapter.GeofenceViewHolder>(GeofenceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofenceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_geofence_profile, parent, false)
        return GeofenceViewHolder(view)
    }

    override fun onBindViewHolder(holder: GeofenceViewHolder, position: Int) {
        val geofence = getItem(position)
        holder.bind(geofence)
    }

    inner class GeofenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textDetails: TextView = itemView.findViewById(R.id.textDetails)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val iconType: ImageView = itemView.findViewById(R.id.iconType)

        fun bind(geofence: GeofenceEntity) {
            textName.text = geofence.name
            
            val typeStr = if (geofence.type == GeofenceType.CIRCLE) "Circle" else "Polygon"
            val mutes = mutableListOf<String>()
            if (geofence.muteMicrophone) mutes.add("Mic")
            if (geofence.muteMedia) mutes.add("Media")
            if (geofence.muteNotification) mutes.add("Notif")
            
            val muteStr = if (mutes.isEmpty()) "No Mutes" else "Mutes: ${mutes.joinToString(", ")}"
            val detailStr = "$typeStr | $muteStr"
            textDetails.text = detailStr

            btnDelete.setOnClickListener { onDeleteClick(geofence) }
            
            iconType.setImageResource(
                if (geofence.type == GeofenceType.CIRCLE) android.R.drawable.ic_menu_mylocation 
                else android.R.drawable.ic_menu_directions
            )
        }
    }

    class GeofenceDiffCallback : DiffUtil.ItemCallback<GeofenceEntity>() {
        override fun areItemsTheSame(oldItem: GeofenceEntity, newItem: GeofenceEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeofenceEntity, newItem: GeofenceEntity): Boolean {
            return oldItem == newItem
        }
    }
}