package com.broads.bleep.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.broads.bleep.R
import com.broads.bleep.entities.Bleep
import com.broads.bleep.inflate
import com.broads.bleep.services.BleepInputMethodService
import kotlinx.android.synthetic.main.bleep_item_row.view.*

class BleepsRecyclerAdapter(private val context: Context, private val bleeps: ArrayList<Bleep>) : RecyclerView.Adapter<BleepsRecyclerAdapter.BleepHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleepHolder {
        val inflatedView = parent.inflate(R.layout.bleep_item_row, false)
        return BleepHolder(context, inflatedView)
    }

    override fun onBindViewHolder(holder: BleepHolder, position: Int) {
        val itemBleep = bleeps[position]
        holder.bindBleep(itemBleep)
    }

    override fun getItemCount(): Int = bleeps.size

    class BleepHolder(private val context: Context, v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var bleep: Bleep? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Log.d("RecyclerView", "CLICK!")
            bleep?.let { (context as BleepInputMethodService).sendBleep(it) }
        }

        fun bindBleep(bleep: Bleep) {
            this.bleep = bleep
            view.bleepTitle.text = bleep.title
        }
    }

}