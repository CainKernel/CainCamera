//
// Created by cain on 2018/1/20.
//

#ifndef CAINCAMERA_QUEUE_H
#define CAINCAMERA_QUEUE_H

// 创建队列
extern int create_queue();
// 销毁队列
extern int destroy_queue();
// 队列是否为空
extern int queue_empty();
// 获取队列大小
extern int queue_size();
// 获取队列索引位置的元素
extern void* queue_get(int index);
// 获取队列头的元素
extern void* queue_get_head();
// 获取队列尾元素
extern void* queue_get_tail();
// 插入新元素
extern int queue_insert(int index, void *pval);
// 从头部插入元素
extern int queue_put_head(void *pval);
// 入队
extern int queue_put_tail(void *pval);
// 删除索引位置的元素
extern int queue_delete(int index);
// 删除队列头节点
extern int queue_remove();
// 删除队列尾节点
extern int queue_remove_last();

#endif //CAINCAMERA_QUEUE_H
