package com.jege.spring.boot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.jege.spring.boot.data.jpa.entity.User;
import com.jege.spring.boot.data.jpa.repository.UserRepository;
import com.jege.spring.boot.json.AjaxResult;

/**
 * @author JE哥
 * @email 1272434821@qq.com
 * @description:用户CRUD操作
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

  @Autowired
  private UserRepository userRepository;

  // 显示用户列表页面
  @RequestMapping("/list")
  public String list() {
    return "user";
  }

  // 从user.jsp列表页面由easyui-datagrid发出ajax请求获取json数据
  @RequestMapping("/json")
  @ResponseBody
  public Map<String, Object> json(@RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "rows", defaultValue = "10") int rows, final String q) {
    // 按照id降序
    Sort sort = new Sort(Sort.Direction.DESC, "id");
    // 封装分页查询条件
    Pageable pageable = new PageRequest(page - 1, rows, sort);
    if (!StringUtils.isEmpty(q)) {
      // 拼接查询条件
      Specification<User> specification = new Specification<User>() {
	@Override
	public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
	  List<Predicate> list = new ArrayList<Predicate>();
	  list.add(cb.like(root.get("name").as(String.class), "%" + q + "%"));
	  Predicate[] p = new Predicate[list.size()];
	  return cb.and(list.toArray(p));

	}
      };
      return findEasyUIData(userRepository.findAll(specification, pageable));
    } else {
      return findEasyUIData(userRepository.findAll(pageable));
    }

  }

  // 处理保存
  @RequestMapping("/save")
  @ResponseBody
  public AjaxResult save(User user) {
    userRepository.save(user);
    return new AjaxResult().success();
  }

  // 处理删除
  @RequestMapping("/delete")
  @ResponseBody
  public AjaxResult delete(Long id) {
    // int a = 1 / 0;
    userRepository.delete(id);
    return new AjaxResult().success();
  }

  // 下载pdf文档
  @RequestMapping("/download")
  public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
    // 告诉浏览器用什么软件可以打开此文件
    response.setHeader("content-Type", "application/pdf");
    // 下载文件的默认名称
    response.setHeader("Content-Disposition", "attachment;filename=user.pdf");

    Document document = new Document();
    PdfWriter.getInstance(document, response.getOutputStream());
    document.open();
    List<User> list = userRepository.findAll();
    for (User user : list) {
      PdfPTable table = new PdfPTable(3);
      PdfPCell cell = new PdfPCell();
      cell.setPhrase(new Paragraph(user.getId().toString()));
      table.addCell(cell);
      document.add(table);

      cell = new PdfPCell();
      cell.setPhrase(new Paragraph(user.getName().toString()));
      table.addCell(cell);
      document.add(table);

      cell = new PdfPCell();
      cell.setPhrase(new Paragraph(user.getAge().toString()));
      table.addCell(cell);
      document.add(table);
    }
    document.close();
  }
}
