import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.SongBeatCreate
import com.example.guitarapp.databinding.FragmentTutorialCreateBinding
import com.example.guitarapp.databinding.ItemSongBeatEditGroupBinding
import com.example.guitarapp.presentation.ui.adapters.BeatEditAdapter

class BeatGroupEditAdapter(
    private val beatGroups: MutableList<MutableList<SongBeatCreate>>,
    private val selectedChords: List<Chord>,
    private val onAddBeat: (groupIndex: Int) -> Unit,
    private val onRemoveBeat: (groupIndex: Int, beatIndex: Int) -> Unit,
    private val onAddGroup: () -> Unit,
    private val onRemoveGroup: (groupIndex: Int) -> Unit,
    private val binding: FragmentTutorialCreateBinding
) : RecyclerView.Adapter<BeatGroupEditAdapter.BeatGroupViewHolder>() {

    inner class BeatGroupViewHolder(val binding: ItemSongBeatEditGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeatGroupViewHolder {
        val itemBinding = ItemSongBeatEditGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BeatGroupViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BeatGroupViewHolder, position: Int) {
        val beatGroup = beatGroups[position]
        holder.binding.rvBeats.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        holder.binding.rvBeats.adapter = BeatEditAdapter(
            beats = beatGroup,
            selectedChords = selectedChords,
            onAddBeat = { onAddBeat(position) },
            onRemoveBeat = { beatIndex -> onRemoveBeat(position, beatIndex) },
            binding = binding
        )

        // Set up the add beat button
        holder.binding.btnAddBeat.setOnClickListener {
            onAddBeat(position)
        }

        // Set up the remove group button
        holder.binding.btnRemoveGroup.setOnClickListener {
            if (beatGroups.size > 1) {
                onRemoveGroup(position)
            } else {
                Toast.makeText(holder.itemView.context, "You must have at least one beat group", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = beatGroups.size

    fun addGroup() {
        beatGroups.add(mutableListOf(SongBeatCreate(null, 0, null, mutableListOf())))
        notifyItemInserted(beatGroups.size - 1)
    }

    fun removeGroup(position: Int) {
        beatGroups.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addBeat(groupIndex: Int) {
        val totalSize = beatGroups.flatten().size
        beatGroups[groupIndex].add(SongBeatCreate(null, totalSize, null, mutableListOf()))
        notifyItemChanged(groupIndex)
    }

    fun removeBeat(groupIndex: Int, beatIndex: Int) {
        beatGroups[groupIndex].removeAt(beatIndex)
        // Update beat numbers
        beatGroups[groupIndex].forEachIndexed { index, beat ->
            beat.beat = index
        }
        notifyItemChanged(groupIndex)
    }
}