package io.github.chenfei0928.widget.recyclerview.binding;

import com.drakeet.multitype.MultiTypeAdapter;

import java.util.List;
import java.util.Map;

/**
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2020-11-01 13:43
 */
public class RecyclerViewBindingUtil {
    public static MultiTypeAdapter getAdapter(AbsRecyclerViewBinding binding) {
        return binding.getAdapter();
    }

    public static List<Object> getList(AbsRecyclerViewBinding binding) {
        return binding.getList();
    }

    public static void addSingleItem(AbsRecyclerViewBinding binding, int position, Object item) {
        if (binding instanceof AbsLayoutParamRecyclerViewBinding) {
            AbsLayoutParamRecyclerViewBinding<AbsLayoutParamRecyclerViewBinding.LayoutParams> spanBinding =
                    (AbsLayoutParamRecyclerViewBinding<AbsLayoutParamRecyclerViewBinding.LayoutParams>) binding;
            spanBinding.addSingleItem(position, item, spanBinding.generateDefaultLayoutParams());
        } else {
            binding.addSingleItem(position, item);
        }
    }

    public static <LP extends AbsLayoutParamRecyclerViewBinding.LayoutParams>
    Map<Object, LP> getLayoutParamsRecord(AbsLayoutParamRecyclerViewBinding<LP> binding) {
        return binding.getLayoutParamsRecord();
    }
}
