# -*- coding: utf-8 -*-
"""复制 yudao-server.jar 并替换 DemoDataServiceImpl.class（外科手术式修复）"""
import shutil
import zipfile

SRC = r"D:\AAA\hotel-management-yudao\yudao-server\target\yudao-server.jar"
DST = r"D:\AAA\hotel-management-yudao\yudao-server\target\yudao-server-t4fix.jar"
ENTRY = "BOOT-INF/classes/cn/iocoder/yudao/module/hotel/service/demo/DemoDataServiceImpl.class"
FIX = r"D:\AAA\hotel-management-yudao\yudao-module-hotel\target\classes\cn\iocoder\yudao\module\hotel\service\demo\DemoDataServiceImpl.class"

shutil.copyfile(SRC, DST)
with open(FIX, "rb") as f:
    fix_bytes = f.read()
print("fix class bytes:", len(fix_bytes))

with zipfile.ZipFile(SRC, "r") as zin, zipfile.ZipFile(DST, "w", zipfile.ZIP_DEFLATED, compresslevel=1) as zout:
    infos = zin.infolist()
    for info in infos:
        data = zin.read(info.filename)
        if info.filename == ENTRY:
            data = fix_bytes
            print("replaced entry:", info.filename)
        zout.writestr(info, data)
print("done ->", DST)
