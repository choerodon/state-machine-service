package io.choerodon.statemachine.infra.utils;

import java.util.List;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;

/**
 * Created by WangZhe@choerodon.io on 2019-06-13.
 * Email: ettwz@hotmail.com
 */
public class PageUtil {
    public static PageInfo buildPageInfoWithPageInfoList(PageInfo pageInfo, List list) {
        Page page = new Page<>(pageInfo.getPageNum(), pageInfo.getPageSize());
        page.setTotal(pageInfo.getTotal());
        page.addAll(list);

        return page.toPageInfo();
    }
}
