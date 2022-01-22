package namelessnet.org.ui.sector

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import namelessnet.org.R

class SectorFragment : Fragment() {

    companion object {
        fun newInstance() = SectorFragment()
    }

    private lateinit var viewModel: SectorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sector_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SectorViewModel::class.java)
        // TODO: Use the ViewModel
    }

}