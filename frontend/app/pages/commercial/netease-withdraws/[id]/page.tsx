// ~/common/netease-product/NeteaseProductDetail.tsx
import React, { useEffect, useState } from 'react';
import { IDUNN_API } from '~/api';
import { deepNullToUndefined } from '~/common/util/null-to-undefined';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import type { NeteaseProduct } from '~/api/generated';
import type { Route } from './+types/page';
import { OrderDisplay, type FetchOrders } from '~/common/netease-order/OrderDisplay';
import { Link } from 'react-router';
import WithdrawDetailView from '~/common/netease-withdraw/WithdrawDetailView';

// 从生成的 API 导入产品类型（假设为 NeteaseProduct）

export function meta({ params }: Route.MetaArgs) {
  return [
    { title: `Netease Project ${params.id}` },
  ];
}

export function clientLoader({ params }: Route.ClientLoaderArgs) {
  return { id: params.id };
}



/**
 * 产品详情页面
 * 路由参数：id
 */
export default function NeteaseProductDetail({ loaderData }: Route.ComponentProps) {
  const { id } = loaderData;
  return <WithdrawDetailView id={String(id)} />;
}