package com.example.guitarapp.presentation.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.data.model.Comment
import com.example.guitarapp.databinding.ItemCommentBinding

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private var onAuthorClick: ((Comment) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnAuthorClickListener(listener: (Comment) -> Unit) {
        onAuthorClick = listener
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.tvCommentAuthor.setOnClickListener {
                onAuthorClick?.invoke(comment)
            }

            binding.tvCommentAuthor.text = comment.author.username
            binding.tvCommentText.text = comment.text
            binding.tvCommentDate.text = comment.createdAt

            if (comment.comments.isNotEmpty()) {
                val nestedAdapter = CommentAdapter()
                binding.rvNestedComments.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvNestedComments.adapter = nestedAdapter
                nestedAdapter.submitList(comment.comments)
                nestedAdapter.setOnAuthorClickListener(onAuthorClick!!)
                binding.rvNestedComments.visibility = View.VISIBLE
            } else {
                binding.rvNestedComments.visibility = View.GONE
            }
        }

    }

    private class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
