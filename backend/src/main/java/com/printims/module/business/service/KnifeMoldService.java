package com.printims.module.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.printims.common.api.PageResult;
import com.printims.module.business.dto.KnifeLabelVO;
import com.printims.module.business.dto.KnifeMoldQuery;
import com.printims.module.business.entity.KnifeMold;

import java.util.List;

/**
 * 刀模服务。
 */
public interface KnifeMoldService extends IService<KnifeMold> {

    PageResult<KnifeMold> pageQuery(KnifeMoldQuery query);

    void saveMold(KnifeMold entity);

    List<KnifeLabelVO> buildLabels(List<Long> ids);
}
