package com.printims.module.business.controller;

import com.printims.common.annotation.Log;
import com.printims.common.api.PageResult;
import com.printims.common.api.R;
import com.printims.module.business.dto.KnifeLabelVO;
import com.printims.module.business.dto.KnifeMoldQuery;
import com.printims.module.business.entity.KnifeMold;
import com.printims.module.business.service.KnifeMoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 刀模管理接口。
 */
@Tag(name = "刀模管理")
@RestController
@RequestMapping("/api/knife-molds")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','EMPLOYEE')")
public class KnifeMoldController {

    private final KnifeMoldService knifeMoldService;

    @Operation(summary = "分页查询")
    @GetMapping
    public R<PageResult<KnifeMold>> page(KnifeMoldQuery query) {
        return R.ok(knifeMoldService.pageQuery(query));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<KnifeMold> get(@PathVariable Long id) {
        return R.ok(knifeMoldService.getById(id));
    }

    @Log("保存刀模")
    @Operation(summary = "保存")
    @PostMapping
    public R<Void> save(@Valid @RequestBody KnifeMold body) {
        knifeMoldService.saveMold(body);
        return R.ok(null);
    }

    @Log("更新刀模")
    @Operation(summary = "更新")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody KnifeMold body) {
        body.setId(id);
        knifeMoldService.saveMold(body);
        return R.ok(null);
    }

    @Log("删除刀模")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        knifeMoldService.removeById(id);
        return R.ok(null);
    }

    @Operation(summary = "标签打印数据")
    @PostMapping("/labels")
    public R<List<KnifeLabelVO>> labels(@RequestBody IdsReq req) {
        return R.ok(knifeMoldService.buildLabels(req.getIds()));
    }

    @Data
    public static class IdsReq {
        private List<Long> ids;
    }
}
