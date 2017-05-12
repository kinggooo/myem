package com.wangnz.myem.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.em.constant.RegConstants;
import com.em.controller.BaseController;
import com.em.entity.admin.EastMoneyResponse;
import com.em.entity.admin.EmptyResponse;
import com.em.entity.register.RegUserBaseInfo;
import com.em.entity.register.RegUserCardImg;
import com.em.service.register.RegUserBaseInfoService;
import com.em.service.register.RegUserCardImgService;
import com.em.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Controller
@RequestMapping("/uploadUserImg")
public class UploadImgController extends BaseController {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("/upload/")
	private String uploadPath;
	@Value("5120000")
	private String maxSize;

	@Autowired
	private RegUserBaseInfoService regUserBaseInfoService;
	@Autowired
	private RegUserCardImgService regUserCardImgService;

	@ExceptionHandler({ Exception.class })
	public String exception(Exception e) {
		log.info("into excepiton handler");
		System.out.println(e.getMessage());
		e.printStackTrace();
		return "exception";
	}


	/*@RequestMapping(value = "/step1", method = RequestMethod.POST)
	public @ResponseBody EastMoneyResponse filesUpload(MultipartFile files[], String userId,
			HttpServletRequest request) {
		String filePath = "";
		RegUserBaseInfo user = regUserBaseInfoService.queryRegUserBaseInfoByUserid(userId);
		if (user == null)
			return encapsule(new EmptyResponse(), -1, "用户不存在");
		List<RegUserCardImg> list = regUserCardImgService.queryRegUserCardImgList(userId);
		if (list != null) {
			for (RegUserCardImg img : list)
				regUserCardImgService.deleteRegUserCardImgById(img.getId());
		}
		String cardno = user.getIdCardNo();
		// 判断file数组不能为空并且长度大于0
		if (files != null && files.length > 0) {
			try {
				// 循环获取file数组中得文件
				for (int i = 0; i < files.length; i++) {
					MultipartFile file = files[i];
					Long size = file.getSize();
					if (size > Long.parseLong(maxSize))
						return encapsule(new EmptyResponse(), -2, "图片太大");
					// 保存文件
					filePath = saveFile(file, request, cardno, i);
					if (!StringUtils.isEmpty(filePath)) {
						RegUserCardImg param = new RegUserCardImg();
						param.setUserid(userId);
						param.setImgstatus("1");// 图片状态正常
						param.setImgurl(filePath);
						param.setImgtype(String.valueOf(i + 1));
						regUserCardImgService.insertRegUserCardImg(param);
					}
				}
			} catch (Exception e) {
				return encapsule(new EmptyResponse(), -3, "图片上传异常");
			}
		}
		user.setApplyStage(RegConstants.REG_STEP_2);
		regUserBaseInfoService.updateRegUserBaseInfo(user);
		return encapsule(new EmptyResponse(), 0, "图片上传成功");
	}

	private String saveFile(MultipartFile file, HttpServletRequest request, String cardno, int index) throws Exception {
		String filePath = "";
		// 判断文件是否为空
		if (!file.isEmpty()) {
			String o = file.getOriginalFilename();
			String suffix = o.substring(o.indexOf("."));// 文件后缀名
			// 文件保存路径
			//String uploadDir = request.getSession().getServletContext().getRealPath("/") + "/upload/" + cardno;
			String uploadDir = PropertyUtil.getProperty("img.path") + cardno;
			filePath = uploadDir + "/" + index + suffix;
			// 如果目录不存在，创建一个目录
			if (!new File(uploadDir).exists()) {
				File dir = new File(uploadDir);
				dir.mkdirs();
			}
			// 转存文件
			file.transferTo(new File(filePath));
			return filePath;
		}
		return filePath;
	}

	@RequestMapping(value = "/step0", method = RequestMethod.POST)
	@ResponseBody
    public EastMoneyResponse getFiles(String files) {
		ImageFile imgFile = new ImageFile();
		imgFile.setFiles(files);
		return encapsule(imgFile, 0,"返回图片信息");
	}

	@SuppressWarnings("serial")
	class ImageFile implements Serializable  , EastMoneyResponse{
		private String files;

		public String getFiles() {
			return files;
		}

		public void setFiles(String files) {
			this.files = files;
		}

	}
	*/

	/**
	 * 上传证件照
	 *
	 * @param files
	 *            用户身份信息
	 */
	@RequestMapping(value = "/step1", method = RequestMethod.POST)
	@ResponseBody
    public EastMoneyResponse filesUpload(String files) {
		if(files.isEmpty()){
			log.info("the request param is null");
			return encapsule(new EmptyResponse(), -1, "数据异常");
		}
		JSONObject obj = (JSONObject) JSON.parse(files);
		String userId = obj.get("userId").toString();
		if(userId.isEmpty()){
			log.info("the userId is null");
			return encapsule(new EmptyResponse(), -1, "数据异常");
		}
		RegUserBaseInfo user = regUserBaseInfoService.queryRegUserBaseInfoByUserid(userId);
		if (user == null){
			log.info("the user id is : " + userId + " but not exists");
			return encapsule(new EmptyResponse(), -1, "用户不存在");
		}
		List<RegUserCardImg> list = regUserCardImgService.queryRegUserCardImgList(userId);
		if (list != null) {
			for (RegUserCardImg img : list)
				regUserCardImgService.deleteRegUserCardImgById(img.getId());
		}
		String cardno = user.getIdCardNo();

		JSONArray jsonArray = (JSONArray) obj.get("files");
		for(Object object : jsonArray){
			JSONObject jsonObj= (JSONObject) object;
			String fileName = jsonObj.get("filename").toString();
			String imgBase64 = jsonObj.get("base64Content").toString();
			String imgtype = jsonObj.getString("imgtype").toString();

			String temp = fileName.substring(0, fileName.lastIndexOf("."));
			String name = fileName.replaceAll(temp, imgtype);
			String filePath =  PropertyUtil.getProperty("img.path") + cardno + "-" + name;
			saveImage(imgBase64, filePath);

			RegUserCardImg param = new RegUserCardImg();
			param.setUserid(userId);
			param.setImgstatus("1");// 图片状态正常
			param.setImgurl(filePath);
			param.setImgtype(imgtype);
			regUserCardImgService.insertRegUserCardImg(param);
		}
		user.setApplyStage(RegConstants.REG_STEP_2);
		regUserBaseInfoService.updateRegUserBaseInfo(user);
		return encapsule(new EmptyResponse(), 0, "图片上传成功");
	}

	@SuppressWarnings("restriction")
	public static boolean saveImage(String imgBase64, String path) {
		if (imgBase64 == null)
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// 解密
			byte[] b = decoder.decodeBuffer(imgBase64);
			// 处理数据
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					b[i] += 256;
				}
			}
			OutputStream out = new FileOutputStream(path);

			out.write(b);
			out.flush();
			out.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
