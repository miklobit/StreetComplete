package de.westnordost.streetcomplete.quests.oneway

import android.os.Bundle
import androidx.annotation.AnyThread
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.StreetSideRotater
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.*
import de.westnordost.streetcomplete.view.image_select.ImageListPickerDialog
import de.westnordost.streetcomplete.view.image_select.Item
import kotlinx.android.synthetic.main.quest_street_side_puzzle.*
import kotlinx.android.synthetic.main.view_little_compass.*

class AddOnewayForm : AbstractQuestFormAnswerFragment<OnewayAnswer>() {

    override val contentLayoutResId = R.layout.quest_oneway
    override val contentPadding = false

    private var streetSideRotater: StreetSideRotater? = null

    private var selection: OnewayAnswer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.getString(SELECTION)?.let { selection = valueOf(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        puzzleView.showOnlyRightSide()
        puzzleView.listener = { showDirectionSelectionDialog() }

        val defaultResId = R.drawable.ic_oneway_unknown
        val defaultTitleId = R.string.quest_street_side_puzzle_select

        puzzleView.setRightSideImageResource(selection?.iconResId ?: defaultResId)
        puzzleView.setRightSideText(resources.getString( selection?.titleResId ?: defaultTitleId ))

        streetSideRotater = StreetSideRotater(puzzleView, compassNeedleView, elementGeometry as ElementPolylinesGeometry)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        selection?.let { outState.putString(SELECTION, it.name) }
    }

    override fun isFormComplete() = selection != null

    override fun onClickOk() {
        applyAnswer(selection!!)
    }

    @AnyThread override fun onMapOrientation(rotation: Float, tilt: Float) {
        streetSideRotater?.onMapOrientation(rotation, tilt)
    }

    private fun showDirectionSelectionDialog() {
        val ctx = context ?: return
        val items = OnewayAnswer.values().map { it.toItem() }
        ImageListPickerDialog(ctx, items, R.layout.labeled_icon_button_cell, 3) { selected ->
            val oneway = selected.value!!
            puzzleView.replaceRightSideImageResource(oneway.iconResId)
            puzzleView.setRightSideText(resources.getString(oneway.titleResId))
            selection = oneway
            checkIsFormComplete()
        }.show()
    }

    companion object {
        private const val SELECTION = "selection"
    }
}

private fun OnewayAnswer.toItem(): Item<OnewayAnswer> = Item(this, iconResId, titleResId)

private val OnewayAnswer.titleResId: Int get() = when(this) {
    FORWARD -> R.string.quest_oneway2_dir
    BACKWARD -> R.string.quest_oneway2_dir
    NO_ONEWAY -> R.string.quest_oneway2_no_oneway
}

private val OnewayAnswer.iconResId: Int get() = when(this) {
    FORWARD -> R.drawable.ic_oneway_lane
    BACKWARD -> R.drawable.ic_oneway_lane_reverse
    NO_ONEWAY -> R.drawable.ic_oneway_lane_no
}
